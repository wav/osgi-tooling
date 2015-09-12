import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging, SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

enablePlugins(SbtKarafPackaging)

version := "0.1.0.SNAPSHOT"

featuresRequired := Map("system" -> "*")

featuresAddDependencies := true

libraryDependencies ++= Seq(
    "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
    FeatureID("org.apache.karaf.features", "standard", "4.0.1"))