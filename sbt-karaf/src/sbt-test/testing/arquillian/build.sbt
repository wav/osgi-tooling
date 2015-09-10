import wav.devtools.sbt.karaf.{KarafDefaults, SbtKaraf}, SbtKaraf.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtKaraf)

version := "0.1.0.SNAPSHOT"

KarafDefaults.arquillianSettings

updateOptions := updateOptions.value.withCachedResolution(true)