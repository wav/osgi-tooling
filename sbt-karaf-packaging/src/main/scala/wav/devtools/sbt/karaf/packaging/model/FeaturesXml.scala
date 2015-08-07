package wav.devtools.sbt.karaf.packaging.model

import java.io.File

import wav.devtools.sbt.karaf.packaging.Util

import scala.util.{Success, Failure, Try}
import scala.xml.{Elem, XML}

// TODO: collect feature depencencies that are not simple elements. e.g. have a configuration.
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

  private[packaging] val featuresSchemas =
    Seq("1.2.0", "1.3.0")
    .map(v => v -> (s"http://karaf.apache.org/xmlns/features/v$v" -> s"org/apache/karaf/features/karaf-features-$v.xsd"))
    .toMap

  private[packaging] val (featuresXsdUrl, featuresXsd) = featuresSchemas("1.3.0")

  private[packaging] def makeFeaturesXml[N <: scala.xml.Node](name: String, elems: Seq[FeaturesElem]): Elem =
    <features xmlns={featuresXsdUrl} name={name}>{
      elems.filterNot(_.isEmpty).map(_.xml)
    }</features>

  private[packaging] def readFeaturesXml[N <: scala.xml.Node](source: N): Seq[FeaturesElem] = {
    val base = source \\ "features"
    val repositories = base \ "repository" collect { case e: Elem => Repository(e.text) }
    val features = base \ "feature" map { feature =>
      val bundles = feature \ "bundle" collect {
        case e: Elem if e.text.startsWith("wrap:") => WrappedBundle(e.text, Map.empty) // TODO
        case e: Elem => Bundle(e.text)
      }
      val featureRefs = feature \ "feature" collect {
        case e: Elem if e.child.length == 1 => FeatureRef(e.text, e.attributes.asAttrMap.get("version"))
      }
      val m = feature.attributes.asAttrMap
      Feature(m("name"), m.get("version"), (bundles ++ featureRefs).toSet)
    }
    repositories ++ features
  }
  
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
  module: sbt.ModuleID,
  artifact: sbt.Artifact,
  file: File) {
  
  lazy val mavenUrl: MavenUrl =
    MavenUrl(module.organization, artifact.name, module.revision, Some(artifact.extension), artifact.classifier)

  lazy val url: String =
    artifact.url.map(_.toString) getOrElse mavenUrl.toString

  lazy val isOSGiBundle: Boolean =
    if (artifact.extension != "jar") false
    else Util.getJarManifest(file.toString)
      .getMainAttributes
      .containsKey("Bundle-SymbolicName")

  lazy val isFeaturesRepository: Boolean =
    if (artifact.extension != "xml" || artifact.classifier != Some("features")) false
    else Try(Util.validateXml(file.getCanonicalPath, getClass.getResourceAsStream("/" + FeaturesXml.featuresXsd))) match {
      case Failure(ex) => println(ex); false
      case Success(_) => true
    }

  lazy val featuresElems = FeaturesXml.readFeaturesXml(XML.loadFile(file))

  lazy val featureRepositories: Set[String] =
    if (!isFeaturesRepository) Set.empty
    else featuresElems.collect { case Repository(url) => url }.toSet

  lazy val features: Set[FeatureRef] =
    if (!isFeaturesRepository) Set.empty
    else featuresElems.collect { case Feature(name, version, _) => FeatureRef(name, version) }.toSet

  lazy val dependencies: Set[FeatureDependency] =
    if (!isFeaturesRepository) Set.empty
    else featuresElems.collect { case f: Feature => f.deps }.flatten.toSet

}