import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging
import SbtKarafPackaging.autoImport._
import KarafPackagingKeys._
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._

scalaVersion in ThisBuild := "2.11.7"

version in ThisBuild := "0.1.0.SNAPSHOT"

lazy val projectA = project.in(file("A"))
    .enablePlugins(SbtKarafPackaging)

lazy val projectB = project.in(file("B"))
    .enablePlugins(SbtKarafPackaging)

lazy val root = project.in(file("."))
    .enablePlugins(SbtKarafPackaging)
    .dependsOn(projectA, projectB) // generate upstream features.
    .settings(
        featuresProjectFeature := {
            val pf = featuresProjectFeature.value
            pf.copy(deps = pf.deps ++ Set(
                FeatureRef("scr"),
                (featuresProjectFeature in projectA).value.toRef,
                (featuresProjectFeature in projectB).value.toRef
            ))
        })

lazy val checkFeaturesXml = taskKey[Unit]("Tests if the features.xml file was added.")

checkFeaturesXml := {
	if (!(crossTarget.value / "features.xml").exists) {
		sys.error("Couldn't find features.xml")
	}
}