package wav.devtools.sbt.karaf.packaging.model

import java.io.File

import sbt._
import wav.devtools.sbt.karaf.packaging.Util

import scala.xml.{Elem, XML}

object FeaturesXml {

  sealed trait FeaturesElem {
    private[FeaturesXml] def xml: Elem
    private[FeaturesXml] val isEmpty = false
  }

  case class Repository(url: String) extends FeaturesElem {
    private[model] lazy val xml =
      <repository>{url}</repository>
  }

  sealed trait FeatureDependency {
    private[FeaturesXml] def xml: Elem
    private[FeaturesXml] val isEmpty = false
  }

  sealed trait ABundle extends FeatureDependency {
    val url: String
    private[model] lazy val xml =
      <bundle>{url}</bundle>
  }

  val OsgiVersion = """^(\d+\.)?(\d+\.)?(\*|\d+[A-Z\._]*)$""".r
  case class Bundle(url: String) extends ABundle {
    require(!url.isEmpty, "missing bundle url")
  }

  case class WrappedBundle(baseUrl: String, manifest: Map[String, String]) extends ABundle {
    val url: String =
      "wrap:" + url +
        "$" + manifest.map(e => e._1 + "=" + e._2).mkString("&")
  }

  private def initFeatAttrs(version: Option[String]): Map[String,String] =
    version.map { v => // common error.
      require(OsgiVersion.findFirstIn(v).isDefined, s"Invalid Osgi Version: $v")
      Map("version" -> v)
    }.getOrElse(Map())

  case class Feature(name: String, version: Option[String] = None, deps: Set[FeatureDependency] = Set.empty) extends FeaturesElem {
    private lazy val attrs = initFeatAttrs(version).updated("name", name)
    private[model] lazy val xml =
      Util.setAttrs(<feature>
        {deps.filterNot(_.isEmpty).map(_.xml)}
      </feature>, attrs)
    private[FeaturesXml] override val isEmpty = name == null
    lazy val toRef = FeatureRef(name, version)
  }

  val emptyFeature = Feature(null)

  case class FeatureRef(name: String, version: Option[String] = None) extends FeatureDependency {
    private lazy val attrs = initFeatAttrs(version)
    private[model] lazy val xml =
      Util.setAttrs(<feature>{name}</feature>, attrs)
    private[FeaturesXml] override val isEmpty = name == null
  }

  val emptyFeatureRef = FeatureRef(null)

  private[packaging] val XMLNS = "http://karaf.apache.org/xmlns/features/v1.2.0"
  private[packaging] val XSD = "org/apache/karaf/features/karaf-features-1.2.0.xsd"

  private[packaging] def toXml(name: String, elems: Seq[FeaturesElem], target: Option[File] = None): Elem =
    <features xmlns={XMLNS} name={name}>
      {target.filter(_.exists).map { target =>
      XML.loadFile(target) \\ "features" match {
        case Elem(prefix, label, attribs, scope, _, existingElements @ _*) =>
          Elem(prefix, label, attribs, scope, true, existingElements ++ elems.map(_.xml): _*)
      }
    }.getOrElse(elems.filterNot(_.isEmpty).map(_.xml))}
    </features>

}

import FeaturesXml._

case class MavenUrl(groupId: String, artifactId: String, version: String, `type`: Option[String] = None, classifer: Option[String] = None) {
  override def toString: String = {
    val url = s"mvn:$groupId/$artifactId/$version"
    (`type`, classifer) match {
      case (Some(t), Some(c)) => s"$url/$t/$c"
      case (Some(t), None) => s"$url/$t"
      case (None, Some(c)) => s"$url//$c"
      case _ => url
    }
  }
}

// An artifact that is resolved for a features file.
case class FeaturesArtifact(
  file: File,
  url: Option[String],
  groupId: String,
  artifactId: String,
  version: String, // REVIEW: should validate against FeaturesXml.OsgiVersion
  `type`: String,
  ext: String,
  classifier: Option[String]) {

  def toMavenUrl: MavenUrl =
    MavenUrl(groupId, artifactId, version, Some(ext), classifier)

  def toUrl: String =
    url getOrElse toMavenUrl.toString
}

object FeaturesArtifact {

  def isBundle(fa: FeaturesArtifact): Boolean = {
    if (fa.ext != "jar") return false
    var manifest = Util.getJarManifest(fa.file.toString)
    manifest.getMainAttributes.containsKey("Bundle-SymbolicName")
  }

  def toRepository(fa: FeaturesArtifact): Option[Repository] =
    if (fa.ext == "xml" && fa.classifier == Some("features")) Some(Repository(fa.toUrl)) else None

  def toBundle(fa: FeaturesArtifact): Option[ABundle] =
    if (fa.ext == "jar" && (fa.`type` == "jar" || fa.`type` == "bundle")) {
      val bundle = if (isBundle(fa)) Bundle(fa.toUrl)
      else WrappedBundle(fa.toUrl, Map(
        "Bundle-SymbolicName" -> (fa.groupId + "." + fa.artifactId).replace("-", "."),
        "Bundle-Version" -> fa.version
      ))
      Some(bundle)
    } else None

}