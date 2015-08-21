import wav.devtools.sbt.karaf.SbtKaraf, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtOsgi, SbtKaraf)

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