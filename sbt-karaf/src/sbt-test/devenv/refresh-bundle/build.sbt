import wav.devtools.sbt.karaf.{packaging, SbtKaraf}, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtOsgi, SbtKaraf)

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

featuresRequired := Map("wrap" -> "*")

osgiSettings

OsgiKeys.exportPackage := Seq(organization.value + ".refreshbundle")

OsgiKeys.importPackage := Seq("scala", "scala.*")

libraryDependencies ++= Seq(
	"org.json" % "json" % "20140107", // This is not a bundle. It's been added to test the wrap protocol
	"org.slf4j" % "slf4j-api" % "1.7.10",
	"org.osgi" % "org.osgi.core" % "5.0.0",
	FeatureID("org.apache.karaf.features", "standard", "4.0.1"))

logLevel := Level.Warn