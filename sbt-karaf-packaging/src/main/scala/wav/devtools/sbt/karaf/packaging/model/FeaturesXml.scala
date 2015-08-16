package wav.devtools.sbt.karaf.packaging.model

import wav.devtools.sbt.karaf.packaging.Util

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

  private def compareVersion(a: String, b: String): Int = {
    var as = a.split('.').toSeq
    var bs = b.split('.').toSeq

    if (as.size > bs.size) bs = bs ++ (1 to (as.size - bs.size)).map(_ => "")
    else if (as.size < bs.size) as = as ++ (1 to (bs.size - as.size)).map(_ => "")

    if (as == bs) 0
    else {
      var i = 0
      while (i < as.size) {
        if (as(i) > bs(i)) return 1
        if (as(i) < bs(i)) return -1
        i += 1
      }
      0
    }
  }

  case class FeatureRef(name: String, version: Option[String] = None) extends FeatureDependency with Ordered[FeatureRef] {
    private lazy val attrs = initFeatAttrs(version)
    private[model] lazy val xml =
      Util.setAttrs(<feature>{name}</feature>, attrs)
    private[FeaturesXml] override val isEmpty = name == null
    override def toString(): String =
      if (version.isEmpty) name else s"$name:${version.get}"
    def compare(that: FeatureRef): Int =
      compareVersion(this.version.getOrElse(""), that.version.getOrElse(""))
  }

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

  private[packaging] def readFeaturesXml[N <: scala.xml.Node](source: N): Option[FeaturesXml] = {
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
    val Some((n, v)) = base collectFirst {
      case e: Elem => (e.attributes.asAttrMap("name"), e.attributes.asAttrMap.get("version"))
    }
    val fxml = new FeaturesXml(n, v, repositories ++ features)
    if (fxml.elems.nonEmpty) Some(fxml) else None
  }

}

case class FeaturesXml(name: String, version: Option[String] = None, elems: Seq[FeaturesXml.FeaturesElem] = Nil)