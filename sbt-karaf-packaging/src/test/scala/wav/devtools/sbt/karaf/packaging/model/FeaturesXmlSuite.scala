package wav.devtools.sbt.karaf.packaging.model

import org.scalatest.Spec
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._

import sbt.IO

class FeaturesXmlSuite extends Spec {

  val karafStandardFeatures: sbt.ModuleID = {
    import sbt._
    "org.apache.karaf.features" % "standard" % "4.0.0" classifier("features")
  }

  def karafStandardFeaturesStream =
    getClass.getResourceAsStream("/org/apache/karaf/features/standard/4.0.0/standard-4.0.0-features.xml")

  def karafStandardFeaturesXml =
    scala.xml.XML.load(karafStandardFeaturesStream)

  def karafStandardFeaturesFile =
    IO.withTemporaryFile("standard-4.0.0-features_", ".xml") { f =>
      IO.write(f, IO.readStream(karafStandardFeaturesStream))
      f
    }

  def karafFeaturesArtifact =
    FeaturesArtifact(
      karafStandardFeatures,
      sbt.Artifact("org.apache.karaf.features.standard", "xml", "xml", "features"),
      karafStandardFeaturesFile)

  lazy val featuresElems = readFeaturesXml(karafStandardFeaturesXml)

  def `should read valid feature files`(): Unit = {
    val paxRepo = Repository("mvn:org.ops4j.pax.web/pax-web-features/4.1.4/xml/features")
    assert(featuresElems.contains(paxRepo))
    val jolokia = Feature("jolokia",Some("1.3.0"),Set(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"), FeatureRef("http",None)))
    assert(featuresElems.contains(jolokia))
  }

  def `should identify feature that is defined but not referenced`(): Unit =
    karafFeaturesArtifact.features.contains(FeatureRef("jolokia",Some("1.3.0")))

  def `should identify a feature dependency that is not defined but referenced`(): Unit =
    karafFeaturesArtifact.dependencies.contains(FeatureRef("pax-http", None))

  def `should identify a bundle dependency`(): Unit =
    karafFeaturesArtifact.dependencies.contains(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"))

  def `syntax test`(): Unit = {
    val syntax = new ModuleSyntax {}
    import syntax._
    import sbt._

    val featuresDependencies = Seq[FeaturesXmlModuleID]() // like setting.

    // REVIEW: Add directives to ModuleID.wrapBundle like `use bnd` some how.
    //         But later, wrapping is a low priority atm.

    featuresDependencies ++ Seq(
      "org.example" % "has-unwrapped-artifact" % "4.0.0" wrapBundle(
        artifactFilter(name="unwrapped") -> Map(
            "Bundle-SymbolicName" -> "unwrapped",
            "Bundle-Version" -> "version"
        )),
      "org.apache.karaf.features" % "standard" % "4.0.0" features("scr"))

  }

}
