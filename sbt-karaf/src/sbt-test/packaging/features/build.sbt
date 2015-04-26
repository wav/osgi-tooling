import wav.devtools.sbt.karaf.SbtKaraf

enablePlugins(SbtKaraf)

lazy val checkFeaturesXml = taskKey[Unit]("Tests if the features.xml file was added.")

checkFeaturesXml := {
	import java.nio.file.Files
	if (!(crossTarget.value / "features.xml").exists) {
		sys.error("Couldn't find features.xml")
	}
}