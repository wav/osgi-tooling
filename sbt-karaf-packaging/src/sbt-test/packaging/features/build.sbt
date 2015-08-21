import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging
import SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

enablePlugins(SbtKarafPackaging)

featuresRequired := Map("jolokia" -> "1.3.0", "scr" -> "*")

libraryDependencies ++= {
  import wav.devtools.sbt.karaf.Dependencies.Karaf
  Seq(
    "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
    Karaf.standardFeatures,
    Karaf.paxWebFeatures)
}

lazy val checkJolokiaIsAvailable = taskKey[Unit]("Tests if the jolokia feature was resolved")

checkJolokiaIsAvailable := {
  val Right(resolved) = featuresSelected.value
  if (!resolved.map(_.name).contains("jolokia"))
    sys.error("The jolokia feature was not resolved")
}

lazy val checkBundleAndFeatureIncluded = taskKey[Unit]("Tests if the jolokia feature and slf4j bundle is included in the the project feature")

checkBundleAndFeatureIncluded := {
  val deps = featuresProjectFeature.value.deps
  import wav.devtools.sbt.karaf.packaging.model, model.MavenUrl, model.FeaturesXml._
  val jolokiaResult = deps.collectFirst { case ref @ FeatureRef("jolokia", _) => ref }
  if (jolokiaResult.isEmpty)
    sys.error("The jolokia feature was not added to the project feature")
  val logger = streams.value.log
  logger.info(deps.toString)
  val slf4jResult = deps
    .collect { case Bundle(MavenUrl(url)) => url }
    .collectFirst { case MavenUrl("org.slf4j", "osgi-over-slf4j-1.7.10", "1.7.10", Some("bundle"), None) => true }
  // REVIEW: update report produces: osgi-over-slf4j-1.7.10, expecting: osgi-over-slf4j
  if (slf4jResult.isEmpty) 
    sys.error("The slf4j bundle was not added to the project feature")
}

lazy val checkFeaturesXml = taskKey[Unit]("Tests if the features.xml file was added.")

checkFeaturesXml := {
	if (!(crossTarget.value / "features.xml").exists)
		sys.error("Couldn't find features.xml")
}

version := "0.1.0.SNAPSHOT"

updateOptions := updateOptions.value.withCachedResolution(true)