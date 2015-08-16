import wav.devtools.sbt.karaf.packaging.{SbtKarafPackaging, FeaturesRepositoryID, model}
import SbtKarafPackaging.autoImport._

enablePlugins(SbtKarafPackaging)

featuresRequired := Map("jolokia" -> "1.3.0", "scr" -> "*") // version ranges and None not implemented.

libraryDependencies += "org.slf4j" % "osgi-over-slf4j" % "1.7.10"

libraryDependencies ++= Seq(
  FeaturesRepositoryID("org.apache.karaf.features", "standard", "4.0.0"),
  FeaturesRepositoryID("org.apache.karaf.features", "enterprise", "4.0.0"))

lazy val checkJolokiaIsAvailable = taskKey[Unit]("Tests if the jolokia feature was resolved")

checkJolokiaIsAvailable := {
  val selection = featuresSelected.value.map(_.name)
  if (!selection.contains("jolokia"))
    sys.error("The jolokia feature was not resolved")
}

lazy val checkBundleAndFeatureIncluded = taskKey[Unit]("Tests if the jolokia feature and slf4j bundle is included in the the project feature")

checkBundleAndFeatureIncluded := {
  val deps = featuresProjectFeature.value.deps
  import model.{FeaturesXml => FX, MavenUrl}
  val jolokiaResult = deps.collectFirst { case ref @ FX.FeatureRef("jolokia", _) => ref }
  if (jolokiaResult.isEmpty)
    sys.error("The jolokia feature was not added to the project feature")
  val logger = streams.value.log
  logger.info(deps.toString)
  val slf4jResult = deps
    .collect { case b: FX.Bundle => MavenUrl.unapply(b.url) }
    .collectFirst { case Some(MavenUrl("org.slf4j", "osgi-over-slf4j", "1.7.10", Some("bundle"), None)) => true }
  // FIX: failing, update report produces: osgi-over-slf4j-1.7.10, expecting: osgi-over-slf4j
  if (slf4jResult.isEmpty) 
    sys.error("The slf4j bundle was not added to the project feature")
}

lazy val checkFeaturesXml = taskKey[Unit]("Tests if the features.xml file was added.")

checkFeaturesXml := {
	if (!(crossTarget.value / "features.xml").exists)
		sys.error("Couldn't find features.xml")
}

version := "0.1.0.SNAPSHOT"