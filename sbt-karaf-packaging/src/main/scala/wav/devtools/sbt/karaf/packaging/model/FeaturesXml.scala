package wav.devtools.sbt.karaf.packaging.model

import org.osgi.framework.{Version, VersionRange}
import wav.devtools.sbt.karaf.packaging.Util

import scala.collection.mutable.{Map => MMap}
import scala.xml.Elem

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

  abstract class ABundle extends FeatureDependency {
    val url: String
    private[model] lazy val xml =
      <bundle>{url}</bundle>
  }

  case class Bundle(url: String) extends ABundle {
    require(!url.isEmpty, "missing bundle url")
  }

  case class WrappedBundle(baseUrl: String, manifest: Map[String, String]) extends ABundle {
    val url: String =
      "wrap:" + url +
        "$" + manifest.map(e => e._1 + "=" + e._2).mkString("&")
  }

  case class Feature(name: String, version: Version = Version.emptyVersion, deps: Set[FeatureDependency] = Set.empty) extends FeaturesElem {
    private[model] lazy val xml =
      Util.setAttrs(<feature>
        {deps.filterNot(_.isEmpty).map(_.xml)}
      </feature>, Map("name" -> name, "version" -> version.toString()))
    private[FeaturesXml] override val isEmpty = name == null
    lazy val toRef = FeatureRef(name, if (version == Version.emptyVersion) None else Some(VersionRange.valueOf(version.toString())))
    lazy val bundles: Set[ABundle] = deps.collect { case b: ABundle => b }
    lazy val featureRefs: Set[FeatureRef] = deps.collect { case ref: FeatureRef => ref }
  }

  def feature(name: String, version: String, deps: Set[FeatureDependency] = Set.empty): Feature =
      Feature(name, Version.parseVersion(version), deps)

  val emptyFeature = Feature(null)

  case class FeatureRef(name: String, version: Option[VersionRange] = None) extends FeatureDependency {
    private lazy val attrs = {
      val m = MMap[String, String]()
      version.foreach(v => m.put("version", v.toString()))
      m.toMap
    }
    private[model] lazy val xml =
      Util.setAttrs(<feature>{name}</feature>, attrs)
    private[FeaturesXml] override val isEmpty = name == null
    override def toString(): String =
      if (version.isEmpty) name else s"$name:${version}"
  }

  def featureRef(name: String, version: String): FeatureRef =
      FeatureRef(name, Some(VersionRange.valueOf(version)))

  val emptyFeatureRef = FeatureRef(null)

  private[packaging] val featuresSchemas =
    Seq("1.2.0", "1.3.0")
    .map(v => v -> (s"http://karaf.apache.org/xmlns/features/v$v" -> s"org/apache/karaf/features/karaf-features-$v.xsd"))
    .toMap

  private[packaging] val (featuresXsdUrl, featuresXsd) = featuresSchemas("1.3.0")

  private[packaging] def makeFeaturesXml[N <: scala.xml.Node](featuresXml: FeaturesXml): Elem =
    <features xmlns={featuresXsdUrl} name={featuresXml.name}>{
      featuresXml.elems.filterNot(_.isEmpty).map(_.xml)
    }</features>

  // TODO: read dependency attribute.
  private[packaging] def readFeaturesXml[N <: scala.xml.Node](source: N): Option[FeaturesXml] = {
    val base = source \\ "features"
    val repositories = base \ "repository" collect { case e: Elem => Repository(e.text) }
    val features = base \ "feature" map { feature =>
      val bundles = (feature \ "bundle" collect {
        case e: Elem if e.text.startsWith("wrap:") => WrappedBundle(e.text, Map.empty) // TODO
        case e: Elem => Bundle(e.text)
      }).toSet
      val featureRefs = (feature \ "feature" collect {
        case e: Elem if e.child.length == 1 => FeatureRef(e.text, e.attributes.asAttrMap.get("version").map(VersionRange.valueOf))
      }).toSet
      val m = feature.attributes.asAttrMap
      Feature(m("name"), m.get("version").map(Version.parseVersion).getOrElse(Version.emptyVersion), bundles ++ featureRefs)
    }
    val Some(n) = base collectFirst {
      case e: Elem => e.attributes.asAttrMap("name")
    }
    val fxml = new FeaturesXml(n, repositories ++ features)
    if (fxml.elems.nonEmpty) Some(fxml) else None
  }

}

case class FeaturesXml(name: String, elems: Seq[FeaturesXml.FeaturesElem] = Nil)