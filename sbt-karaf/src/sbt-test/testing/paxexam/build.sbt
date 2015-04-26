import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.OsgiKeys._
import wav.devtools.sbt.karaf.SbtKaraf
import wav.devtools.sbt.karaf.Dependencies.`slf4j-simple`

enablePlugins(SbtKaraf)

SbtOsgi.osgiSettings

SbtKaraf.paxSettings

libraryDependencies ++= Seq(`slf4j-simple`)

importPackage := Seq(
	"org.osgi.framework",
	"scala",
	"scala.*")