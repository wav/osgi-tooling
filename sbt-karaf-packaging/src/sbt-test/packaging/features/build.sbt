import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging, SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

enablePlugins(SbtKarafPackaging)

version := "0.1.0.SNAPSHOT"

featuresRequired := Set(
  feature("jolokia", "1.3.0"),
  feature("scr"),
  feature("wrap", dependency = true))

libraryDependencies ++=
  Seq(
    "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
    FeatureID("org.apache.karaf.features", "standard", "4.0.2"),
    FeatureID("org.ops4j.pax.web", "pax-web-features", "4.1.4"))

lazy val checkJolokiaIsAvailable = taskKey[Unit]("Tests if the jolokia feature was resolved")

checkJolokiaIsAvailable := {
  val Right(resolved) = featuresSelected.value
  if (!resolved.map(_.name).contains("jolokia"))
    sys.error("The jolokia feature was not resolved")
}

lazy val checkBundleAndFeatureIncluded = taskKey[Unit]("Tests if the jolokia feature and slf4j bundle is included in the the project feature")

checkBundleAndFeatureIncluded := {
  val deps = featuresProjectFeature.value.deps
  import FeaturesXml._
  val jolokiaResult = deps.collectFirst { case ref @ Dependency("jolokia", _, _, _) => ref }
  if (jolokiaResult.isEmpty)
    sys.error("The jolokia feature was not added to the project feature")
  val logger = streams.value.log
  logger.info(deps.toString)
  val slf4jResult = deps
    .collect { case Bundle(MavenUrl(url), _, _, _, _) => url }
    .collectFirst { case MavenUrl("org.slf4j", "osgi-over-slf4j", "1.7.10", None, None) => true }
  if (slf4jResult.isEmpty) 
    sys.error("The slf4j bundle was not added to the project feature")
}

lazy val checkFeaturesXml = taskKey[Unit]("Tests if the features.xml file was added.")

checkFeaturesXml := {
	if (!(crossTarget.value / "features.xml").exists)
		sys.error("Couldn't find features.xml")
}