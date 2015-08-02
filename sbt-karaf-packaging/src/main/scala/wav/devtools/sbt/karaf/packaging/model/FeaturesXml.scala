package wav.devtools.sbt.karaf.packaging.model

import java.io.File

import wav.devtools.sbt.karaf.packaging.Util

import scala.collection.mutable
import scala.xml.{Elem, XML}

object FeaturesXml {

  abstract class FeatureElem {
    private[FeaturesXml] def xml: Elem
    private[FeaturesXml] val isEmpty = false
  }

  case class Bundle(url: String) extends FeatureElem {
    require(!url.isEmpty, "missing bundle url")
    private[model] lazy val xml =
      <bundle>{url}</bundle>
  }

  val OsgiVersion = """^(\d+\.)?(\d+\.)?(\*|\d+[A-Z\._]*)$""".r

  private def initFeatAttrs(version: Option[String]): Map[String,String] =
    version.map { v => // common error.
      require(OsgiVersion.findFirstIn(v).isDefined, s"Invalid Osgi Version: $v")
      Map("version" -> v)
    }.getOrElse(Map())

  case class Feature(name: String, version: Option[String] = None, elems: Set[FeatureElem] = Set.empty) {
    private lazy val attrs = initFeatAttrs(version).updated("name", name)
    private[model] lazy val xml =
      Util.setAttrs(<feature>
        {elems.filterNot(_.isEmpty).map(_.xml)}
      </feature>, attrs)
    private[FeaturesXml] val isEmpty = name == null
    lazy val toRef = FeatureRef(name, version)
  }

  val emptyFeature = Feature(null)

  case class FeatureRef(name: String, version: Option[String] = None) extends FeatureElem {
    private lazy val attrs = initFeatAttrs(version)
    private[model] lazy val xml =
      Util.setAttrs(<feature>{name}</feature>, attrs)
    private[FeaturesXml] override val isEmpty = name == null
  }

  val emptyFeatureRef = FeatureRef(null)

  private[packaging] val XMLNS = "http://karaf.apache.org/xmlns/features/v1.2.0"
  private[packaging] val XSD = "org/apache/karaf/features/karaf-features-1.2.0.xsd"

  private[packaging] def toXml(name: String, features: Seq[Feature], target: Option[File] = None): Elem =
    <features xmlns={XMLNS} name={name}>
      {target.filter(_.exists).map { target =>
      XML.loadFile(target) \\ "features" match {
        case Elem(prefix, label, attribs, scope, _, existingElements@_*) =>
          Elem(prefix, label, attribs, scope, true, existingElements ++ features.map(_.xml): _*)
      }
    }.getOrElse(features.filterNot(_.isEmpty).map(_.xml))}
    </features>

}
