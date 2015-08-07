import wav.devtools.sbt.karaf.{Dependencies => deps}

sbtPlugin := true

scriptedSettings

scriptedLaunchOpts += "-Dproject.version=" + version.value

scalaVersion in ThisBuild := "2.10.5"

crossScalaVersions in ThisBuild := Seq()

fork in scripted := true // this may be required for log output

publishArtifact in Compile := true

publishArtifact in Test := false

libraryDependencies ++= Seq(
  deps.scalaTest,
  deps.commonsLang,
  deps.slf4j)