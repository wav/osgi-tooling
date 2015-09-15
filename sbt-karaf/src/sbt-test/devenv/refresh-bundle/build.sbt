import wav.devtools.sbt.karaf.{packaging, SbtKaraf}, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtOsgi, SbtKaraf)

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

osgiSettings

OsgiKeys.exportPackage := Seq(organization.value + ".refreshbundle")

OsgiKeys.importPackage := Seq("scala", "scala.*")

/**
 * Depend on the logging feature.
 */

featuresRequired := Map("log" -> "*")

featuresAddDependencies := true

libraryDependencies += FeatureID("org.apache.karaf.features", "standard", "4.0.0")

logLevel := Level.Warn