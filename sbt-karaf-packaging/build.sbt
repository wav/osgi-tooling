sbtPlugin := true

scriptedSettings

scriptedLaunchOpts += "-Dproject.version=" + version.value

scalaVersion in ThisBuild := "2.10.5"

crossScalaVersions in ThisBuild := Seq()

fork in scripted := true // this may be required for log output

publishArtifact in Compile := true

publishArtifact in Test := false

import wav.devtools.sbt.karaf.Dependencies._

libraryDependencies ++= Seq(
  osgiCore,
  scalaTest,
  commonsLang,
  slf4j)

val features = Seq(
  Karaf.standardFeatures,
  Karaf.enterpriseFeatures,
  Karaf.paxWebFeatures)

libraryDependencies ++= features

managedResources in Test <++= Def.task {
  (for {
    cr <- (update in Test).value.configurations
    if (cr.configuration == "test")
    mr <- cr.modules
    m = mr.module
    (a ,f) <- mr.artifacts
    expected <- features
    if (expected.organization == m.organization)
    if (expected.name == m.name)
    if (a.extension == "xml")
  } yield f).toSet.toSeq
}

updateOptions := updateOptions.value.withCachedResolution(true)