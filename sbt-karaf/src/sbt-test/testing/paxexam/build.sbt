import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.OsgiKeys._
import wav.devtools.sbt.karaf.SbtKaraf

enablePlugins(SbtKaraf)

SbtOsgi.osgiSettings

SbtKaraf.paxSettings

importPackage := Seq(
	"org.osgi.framework",
	"scala",
	"scala.*")

version := "0.1.0.SNAPSHOT"