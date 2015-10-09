package wav.devtools.karaf.packaging

import java.io.File

import org.apache.commons.io.FileUtils
import org.scalatest.Spec
import FeaturesXml._

class FeaturesXmlSuite extends Spec {

  val vKaraf = sys.props("karaf.version")
  val vPaxWeb = "4.2.0"
  val vJolokia = "1.3.0"
  val jolokiaUrl = s"mvn:org.jolokia/jolokia-osgi/$vJolokia"
  val paxWebUrl = s"mvn:org.ops4j.pax.web/pax-web-features/$vPaxWeb/xml/features"

  val repoIDs = Map[String, String](
    "standard" -> s"/standard-$vKaraf-features.xml",
    "enterprise" -> s"/enterprise-$vKaraf-features.xml",
    "pax-web" -> "/pax-web-features-4.2.0-features.xml"
  )

  def getDescriptor(s: String): Option[FeaturesXml] =
    repoIDs.get(s).flatMap { path =>
      val xml = io.Source.fromInputStream(getClass.getResourceAsStream(path)).getLines.mkString
      val f = new File(FileUtils.getTempDirectory, path)
      FileUtils.writeStringToFile(f, xml)
      ArtifactUtil.readFeaturesXml(f)
    }

  val jolokia13 = Dependency("jolokia",vJolokia.versionRange,false,false)

  def `should read valid feature files`(): Unit = {
    val Some(d) = getDescriptor("standard")
    val paxRepo = Repository(paxWebUrl)
    assert(d.elems.contains(paxRepo))
    val jolokia = feature("jolokia",vJolokia,Set(Bundle(jolokiaUrl), Dependency("http")))
    val Some(selection) = d.features.find(_.name == "jolokia")
    assert(jolokia.name == selection.name)
    assert(jolokia.version == selection.version)
    assert(jolokia.deps.contains(Bundle(jolokiaUrl)))
    assert(jolokia.deps.contains(Dependency("http")))
  }

  def `should identify feature that is defined but not referenced`(): Unit = {
    val Some(d) = getDescriptor("standard")
    d.features.exists(Resolution.satisfies(jolokia13, _))
  }

  def `should identify a feature dependency that is not defined but referenced`(): Unit = {
    val Some(fr) = getDescriptor("standard")
    fr.features.exists(Resolution.satisfies(Dependency("pax-http"), _))
  }

  def `should identify a bundle dependency`(): Unit = {
    val Some(fr) = getDescriptor("standard")
    fr.features.flatMap(_.deps).contains(Bundle(jolokiaUrl))
  }

  def `parses a maven url`(): Unit = {
    val mvnUrl = MavenUrl("g","a.b", "1", Some("xml"), Some("features"))
    val MavenUrl(result) = mvnUrl.toString
    assert(mvnUrl.toString == result.toString)
    assert(mvnUrl.classifer == result.classifer)
    assert(mvnUrl == result)
    val mvnUrl2 = MavenUrl("a", "b", "c", Some(""), Some("")).toMavenUrl
    assert(mvnUrl2.classifer.isEmpty)
    assert(mvnUrl2.`type`.isEmpty)
    assert(!mvnUrl2.toString.endsWith("/"))
  }

  def `features versions and constraints are comparable`(): Unit = {
    val jolokia13vr = Dependency("jolokia","[1.3,1.4)".versionRange)
    assert(jolokia13 == Dependency("jolokia",vJolokia.versionRange))
    assert(Resolution.satisfies(jolokia13vr, feature("jolokia", vJolokia)))
    assert(!Resolution.satisfies(jolokia13vr, feature("jolokia", "1.2.0")))
    assert(!Resolution.satisfies(jolokia13vr, feature("jolokia", "1.4.0")))
  }

  def `finds all transitive features and bundles`(): Unit = {
    val repositories = repoIDs.keys.flatMap(getDescriptor).toSet
    val jolokiaDeps = Resolution.selectFeatureDeps(jolokia13, repositories.flatMap(_.features))
    assert(Set(Dependency("http","0.0.0".versionRange)) == jolokiaDeps)
    val result = Resolution.resolveFeatures(Set(jolokia13), repositories.flatMap(_.features))
    assert(result.isRight)
    val Right(rs) = result
    assert(Set("jolokia", "http", "pax-http", "pax-http-jetty", "pax-jetty") == rs.map(_.name))
    rs.flatMap(_.deps).collect { case Bundle(MavenUrl(url), _, _, _, _) => url }.foreach(println)
  }
  
  def makeDescriptor = {
    val repository = Repository(paxWebUrl)
    val bundle = Bundle("mvn:org.scala-lang/scala-library/2.11.7")
    val bundle2 = Bundle("mvn:org.json/json/20140107")
    val config = Config("my.project.cfg", "property=value")
    val configFile = ConfigFile("my.project.bootstrap.cfg", "https://internal.ip/my.project/bootstrap.cfg")
    val feature = Feature(name = "test-feature", deps = Set(bundle, bundle2, config, configFile))
    FeaturesXml("test-project", Seq(repository, feature))
  }

  def `can write a valid features descriptor`(): Unit = {
    val xml = FeaturesXmlFormats.makeFeaturesXml(makeDescriptor)
    println(xml)
    Util.withTemporaryDirectory { dir =>
      val f = new File(dir, "features.xml")
      Util.write(f, FeaturesXmlFormats.featuresXsd, xml)
    }
  }

  def `modify a feature`(): Unit = {
    val f: Feature = makeDescriptor.elems.collectFirst { case f: Feature => f }.get
    type FeatureMod = PartialFunction[FeatureOption, FeatureOption]
    type BundleMod = PartialFunction[FeatureOption, Bundle]

    def modBundle(predicate: MavenUrl => Boolean, f: Bundle => Bundle): BundleMod = {
      case b @ Bundle(MavenUrl(url), _, _, _, _) if predicate(url) => f(b)
    }

    val wrapJson = modBundle(url => url.artifactId == "json", b => WrappedBundle(b.url, instructions = Map(
      "Bundle-SymbolicName" -> "org.json"
    )))

    f.copy(deps = f.deps.map(wrapJson orElse { case o => o } : FeatureMod))
  }

  def `parses a pax wrap url`(): Unit = {
    val P = FeaturesXml.WrappedBundlePattern
    val inst = "$Bundle-SymbolicName=test"
    val bnd = ",http://test.bnd"
    val P(url1, bnd1, inst1) = "wrap:file:///test.file"
    assert(bnd1 == null && inst1 == null)
    val P(url2, bnd2, inst2) = s"wrap:file:///test.file$bnd"
    assert(bnd2 == bnd && inst2 == null)
    val P(url3, bnd3, inst3) = s"wrap:file:///test.file$inst"
    assert(bnd3 == null && inst3 == inst)
    val P(url4, bnd4, inst4) = s"wrap:file:///test.file$bnd$inst"
    assert(bnd4 == bnd && inst4 == inst)
  }

}