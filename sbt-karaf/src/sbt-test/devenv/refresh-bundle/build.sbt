import wav.devtools.sbt.karaf.{packaging, SbtKaraf}, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtOsgi, SbtKaraf)

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

featuresRequired := Map("wrap" -> "*")

osgiSettings

OsgiKeys.exportPackage := Seq("wav.devtools.sbt.karaf.examples.refreshbundle")

OsgiKeys.privatePackage := Seq("wav.devtools.sbt.karaf.examples.refreshbundle.impl")

OsgiKeys.importPackage := Seq("scala", "scala.*", "org.json", "org.slf4j", "org.osgi.framework")

OsgiKeys.bundleActivator := Option("wav.devtools.sbt.karaf.examples.refreshbundle.Activator")

libraryDependencies ++= Seq(
	"org.json" % "json" % "20140107" toWrappedBundle(Map(
		"Bundle-SymbolicName" -> "json",
		"Bundle-Version" -> "20140107"
	)),
	"org.slf4j" % "slf4j-api" % "1.7.12",
	"org.slf4j" % "osgi-over-slf4j" % "1.7.12",
	"org.osgi" % "org.osgi.core" % "6.0.0",
	FeatureID("org.apache.karaf.features", "standard", "4.0.1"))

logLevel := Level.Warn