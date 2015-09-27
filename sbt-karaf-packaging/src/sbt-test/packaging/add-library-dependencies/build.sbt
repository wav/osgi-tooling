import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging, SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

enablePlugins(SbtKarafPackaging)

version := "0.1.0.SNAPSHOT"

featuresRequired := Map("system" -> "*")

featuresAddDependencies := true

libraryDependencies ++= Seq(
  "org.json" % "json" % "20140107",
  "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
  FeatureID("org.apache.karaf.features", "standard", "4.0.1"))

lazy val checkDependencyIsWrapped = taskKey[Unit]("Tests to see if the bundle definition uses the wrap protocol for a non-osgi dependency")

checkDependencyIsWrapped := {
  val f = featuresProjectFeature.value
  import FeaturesXml._
  val found = f.deps.exists {
    case Bundle(url, _, _, _) => url == "wrap:mvn:org.json/json/20140107"
    case _ => false
  }
  if (!found) sys.error(s"Failed to wrap dependency:\n + $f")
  val n = f.deps.count {
    case Bundle(url, _, _, _) => url.startsWith("wrap:")
    case _ => false
  }
  if (n != 1) sys.error(s"Failed to wrap a single dependency:\n + $f")
}
