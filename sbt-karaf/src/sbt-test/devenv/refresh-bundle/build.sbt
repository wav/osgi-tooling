import wav.devtools.sbt.karaf.{packaging, SbtKaraf}, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtOsgi, SbtKaraf)

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

featuresRequired := Set(
    feature("wrap", /* enable provisioning of wrapped bundles */
        dependency = true, 
        prerequisite = true),
    feature("log") /* implements slf4j */,
    feature("camel-blueprint"),
    feature("aries-blueprint"))

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
	"org.slf4j" % "slf4j-api" % "1.7.12" % "provided",
	"org.osgi" % "org.osgi.core" % "6.0.0" % "provided",
	FeatureID("org.apache.camel.karaf", "apache-camel", "2.16.0"),
	FeatureID("org.apache.karaf.features", "standard", "4.0.2"))

logLevel := Level.Warn