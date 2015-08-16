package wav.devtools.sbt.karaf.packaging.model

import org.scalatest.Spec
import wav.devtools.sbt.karaf.packaging.Resolution
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._

import sbt.IO

class FeaturesXmlSuite extends Spec {

  val standardFeaturesID: sbt.ModuleID = {
    import sbt._
    "org.apache.karaf.features" % "standard" % "4.0.0" classifier("features")
  }

  def standardFeaturesStream =
    getClass.getResourceAsStream("/org/apache/karaf/features/standard/4.0.0/standard-4.0.0-features.xml")

  def standardFeaturesXmlNode =
    scala.xml.XML.load(standardFeaturesStream)

  def standardFeaturesRepository = IO.withTemporaryFile("standard-4.0.0-features_", ".xml") { f =>
    IO.write(f, IO.readStream(standardFeaturesStream))
    val data = FeaturesArtifact(
      standardFeaturesID,
      sbt.Artifact("org.apache.karaf.features.standard", "xml", "xml", "features"),
      Some(f),
      None)
    val repo = data.toRepository
    assert(repo.isDefined)
    repo.get
  }

  lazy val standardFeaturesXml = {
    val xml = readFeaturesXml(standardFeaturesXmlNode)
    assert(xml.isDefined)
    xml.get
  }

  def `should read valid feature files`(): Unit = {
    val paxRepo = Repository("mvn:org.ops4j.pax.web/pax-web-features/4.1.4/xml/features")
    assert(standardFeaturesXml.elems.contains(paxRepo))
    val jolokia = Feature("jolokia",Some("1.3.0"),Set(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"), FeatureRef("http",None)))
    assert(standardFeaturesXml.elems.contains(jolokia))
  }

  def `should identify feature that is defined but not referenced`(): Unit =
    standardFeaturesRepository.features.contains(FeatureRef("jolokia",Some("1.3.0")))

  def `should identify a feature dependency that is not defined but referenced`(): Unit =
    standardFeaturesRepository.dependencies.contains(FeatureRef("pax-http", None))

  def `should identify a bundle dependency`(): Unit =
    standardFeaturesRepository.dependencies.contains(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"))

  def `the newest feature is chosen from a collection`(): Unit = {
    val http = FeatureRef("http")
    val http4 = FeatureRef("http", Some("4.0.0"))
    assert(Resolution.selectNewest(Set(http), Set(http, http4)) == Set(http4))
    assert(Resolution.selectNewest(Set(http), Set(http4, http)) == Set(http4))
    assert(Resolution.selectNewest(Set(http4), Set(http4, http)) == Set(http4))
    assert(Resolution.selectNewest(Set(http4), Set(http)) == Set(http)) // REVIEW.
    assert(Resolution.selectNewest(Set(FeatureRef("???")), Set(http4, http)) == Set())
  }
  
  def `required features are resolved`(): Unit =
    Resolution.requireAllFeatures(Map("jolokia" -> "1.3.0"), Set(standardFeaturesRepository))

  def `parses a maven url`(): Unit = {
    val mvnUrl = MavenUrl("g","a", "1", Some("xml"), Some("features"))
    val MavenUrl(result) = mvnUrl.toString
    assert(mvnUrl == result)
  }
}