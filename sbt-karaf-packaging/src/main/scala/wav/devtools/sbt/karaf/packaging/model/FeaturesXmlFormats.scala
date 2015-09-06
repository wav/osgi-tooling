package wav.devtools.sbt.karaf.packaging.model

import org.osgi.framework.{Version, VersionRange}

import scala.xml.Elem

private[packaging] trait XmlFormat[T] {
  def write(value: T): Option[Elem]

  def read(elem: Elem): Option[T] =
    this._read
      .andThen(Some(_: T))
      .applyOrElse(elem, (_: Elem) => None)

  val _read: PartialFunction[Elem, T]

}

private[packaging] object FeaturesXmlFormats {

  import wav.devtools.sbt.karaf.packaging.Util.setAttrs
  import FeaturesXml._

  private def opt[T](e: Elem, attr: String, conversion: String => T): Option[T] =
    e.attributes.asAttrMap.get(attr).map(conversion)

  private def get[T](e: Elem, attr: String, conversion: String => T, default: T): T =
    opt(e,attr,conversion).getOrElse(default)

  object repositoryFormat extends XmlFormat[Repository] {
    def write(r: Repository) =
      Some(<repository>{r.url}</repository>)
    val _read: PartialFunction[Elem, Repository] = {
      case e: Elem if e.label == "repository" => Repository(e.text)
    }
  }

  object bundleFormat extends XmlFormat[Bundle] {
    def write(b: Bundle) = {
      Some(setAttrs(<bundle>{b.url}</bundle>, Map(
        "dependency" -> Some(b.dependency.toString),
        "start-level" -> b.`start-level`.map(_.toString)
      )))
    }
    val _read: PartialFunction[Elem, Bundle] = {
      case e: Elem if e.label == "bundle" =>
        Bundle(e.text,
          get(e, "dependency", _.toBoolean, false),
          get(e, "start", _.toBoolean, false),
          opt(e, "start-level", _.toInt))
    }
  }

  object dependencyFormat extends XmlFormat[Dependency] {
    def write(d: Dependency) = {
      Some(setAttrs(<feature>{d.name}</feature>, Map(
        "version" -> d.version.map(_.toString),
        "prerequisite" -> Some(d.prerequisite.toString),
        "dependency" -> Some(d.dependency.toString)
      )))
    }
    val _read: PartialFunction[Elem, Dependency] = {
      case e: Elem if e.label == "feature" =>
        Dependency(e.text,
          opt(e, "version", VersionRange.valueOf),
          get(e, "prerequisite", _.toBoolean, true),
          get(e, "dependency", _.toBoolean, true))
    }
  }

  object configFormat extends XmlFormat[Config] {
    def write(c: Config) = {
      Some(setAttrs(<config>{c.value}</config>, Map(
        "name" -> Some(c.name),
        "append" -> Some(c.append.toString)
      )))
    }
    val _read: PartialFunction[Elem, Config] = {
      case e: Elem if e.label == "config" =>
        Config(
          get(e, "name", identity, null),
          e.text,
          get(e, "append", _.toBoolean, true))
    }
  }

  object configFileFormat extends XmlFormat[ConfigFile] {
    def write(cf: ConfigFile) = {
      Some(setAttrs(<configfile>{cf.value}</configfile>, Map(
        "finalname" -> Some(cf.finalname),
        "overrideValue" -> Some(cf.overrideValue.toString)
      )))
    }
    val _read: PartialFunction[Elem, ConfigFile] = {
      case e: Elem if e.label == "configfile" =>
        ConfigFile(
          get(e, "finalname", identity, null),
          e.text,
          get(e, "overrideValue", _.toBoolean, true))
    }
  }

  object featureFormat extends XmlFormat[Feature] {

    def write(f: Feature) = {
      Some(setAttrs(<feature>{
        f.deps.collect {
          case d: Dependency => dependencyFormat.write(d)
          case b: Bundle => bundleFormat.write(b)
          case c: Config => configFormat.write(c)
        }.flatten
        }</feature>, Map(
        "name" -> Some(f.name),
        "version" -> Some(f.version.toString),
        "description" -> f.description)
      ))
    }

    def _readDep(e: Elem): Option[FeatureOption] =
      dependencyFormat._read
        .orElse(bundleFormat._read)
        .orElse(configFormat._read)
        .andThen(Some(_))
        .applyOrElse(e, (_: Elem) => None)

    val _read: PartialFunction[Elem, Feature] = {
      case e: Elem if e.label == "feature" =>
        Feature(
          get(e, "name", identity, null),
          get(e, "version", Version.parseVersion, Version.emptyVersion),
          e.child.collect { case e: Elem => _readDep(e) }.flatten.toSet,
          opt(e, "description", identity))
    }

  }

  object featuresFormat extends XmlFormat[FeaturesXml] {

    def write(fd: FeaturesXml) = {
      Some(setAttrs(<features>{
        fd.elems.collect {
          case r: Repository => repositoryFormat.write(r)
          case f: Feature => featureFormat.write(f)
        }.flatten
        }</features>, Map(
        "name" -> Some(fd.name),
        "xmlns" -> Some("http://karaf.apache.org/xmlns/features/v1.3.0")
      )))
    }

    def _readDep(e: Elem): Option[FeaturesOption] =
      repositoryFormat._read
        .orElse(featureFormat._read)
        .andThen(Some(_))
        .applyOrElse(e, (_: Elem) => None)

    val _read: PartialFunction[Elem, FeaturesXml] = {
      case e: Elem if e.label == "features" =>
        FeaturesXml(
          get(e, "name", identity, null),
          e.child.collect { case e: Elem => _readDep(e) }.flatten)
    }
  }

  val featuresSchemas =
    Seq("1.2.0", "1.3.0")
      .map(v => v -> (s"http://karaf.apache.org/xmlns/features/v$v" -> s"org/apache/karaf/features/karaf-features-$v.xsd"))
      .toMap

  val (featuresXsdUrl, featuresXsd) = featuresSchemas("1.3.0")

  def makeFeaturesXml[N <: scala.xml.Node](featuresXml: FeaturesXml): Elem =
    featuresFormat.write(featuresXml).get

  def readFeaturesXml[N <: scala.xml.Node](source: N): Option[FeaturesXml] =
    (source \\ "features" collectFirst {
      case e: Elem => featuresFormat.read(e)
    }).flatten

}