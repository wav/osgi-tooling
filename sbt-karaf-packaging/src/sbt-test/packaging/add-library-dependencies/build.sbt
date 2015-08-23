import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging
import SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

enablePlugins(SbtKarafPackaging)

featuresRequired := Map("system" -> "*")

libraryDependencies ++= {
  import wav.devtools.sbt.karaf.Dependencies.Karaf
  Seq(
    "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
    Karaf.standardFeatures)
}

version := "0.1.0.SNAPSHOT"

updateOptions := updateOptions.value.withCachedResolution(true)