import wav.devtools.sbt.karaf.SbtKaraf, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

featuresRequired := Map("log" -> "*")

libraryDependencies ++= Seq(
    "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
    FeatureID("org.apache.karaf.features", "standard", "4.0.0"))

enablePlugins(SbtOsgi, SbtKaraf)

osgiSettings

name := "refreshbundle"

organization := "wav.devtools.sbt.karaf.examples"

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= {
  import wav.devtools.sbt.karaf.Dependencies._
  Seq(
    slf4j % "provided",
    osgiCore % "provided"
  )
}

OsgiKeys.exportPackage := Seq(organization.value + ".refreshbundle")

OsgiKeys.importPackage := Seq("scala", "scala.*")

updateOptions := updateOptions.value.withCachedResolution(true)