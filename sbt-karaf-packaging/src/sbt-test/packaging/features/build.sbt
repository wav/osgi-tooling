import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging

enablePlugins(SbtKarafPackaging)

lazy val checkFeaturesXml = taskKey[Unit]("Tests if the features.xml file was added.")

checkFeaturesXml := {
	if (!(crossTarget.value / "features.xml").exists) {
		sys.error("Couldn't find features.xml")
	}
}

version := "0.1.0.SNAPSHOT"