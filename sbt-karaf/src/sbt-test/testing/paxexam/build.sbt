import wav.devtools.sbt.karaf.{KarafDefaults, SbtKaraf}, SbtKaraf.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtOsgi, SbtKaraf)

version := "0.1.0.SNAPSHOT"

KarafDefaults.paxSettings

OsgiKeys.importPackage := Seq(
	"org.osgi.framework",
	"scala",
	"scala.*")

updateOptions := updateOptions.value.withCachedResolution(true)