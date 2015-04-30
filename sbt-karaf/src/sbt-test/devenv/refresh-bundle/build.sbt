import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.OsgiKeys._
import wav.devtools.sbt.karaf.SbtKaraf
import wav.devtools.sbt.karaf.{Dependencies => deps}

enablePlugins(SbtKaraf)

KarafKeys.karafInstanceArgs := None // TODO: workout why instances get stuck in the Starting state.

SbtOsgi.osgiSettings

libraryDependencies ++= Seq(
	deps.slf4j % "provided",
	deps.osgiCore % "provided"
)

scalaVersion := "2.11.6"

exportPackage := Seq(organization.value + ".refreshbundle")

importPackage := Seq("scala", "scala.*")

name := "refreshbundle"

organization := "wav.devtools.sbt.karaf.examples"

version := "0.1.0.SNAPSHOT"