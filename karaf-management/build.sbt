import wav.devtools.sbt.karaf.{Dependencies => deps}

libraryDependencies ++= Seq(
	deps.commonsLang,
	deps.slf4j,
	deps.osgiCore,
	deps.Karaf.bundle,
	deps.Karaf.config,
	deps.Karaf.features,
	deps.Karaf.instance)