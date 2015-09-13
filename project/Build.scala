import sbt._
import Keys._
import sbt.ScriptedPlugin._
import wav.devtools.sbt.karaf.Dependencies._

object OsgiToolingBuild extends Build {
  
  lazy val `karaf-mbean-wrapper` = project
    .settings(commonSettings: _*)
    .settings(
      publishArtifact in Compile := false,
      libraryDependencies ++= Karaf.common)

  lazy val `sbt-karaf-packaging` = project
    .settings(commonPluginSettings: _*)
    .settings(
      libraryDependencies ++=
        features ++
        Seq(
          jarchivelib,
          osgiCore,
          scalaTest,
          commonsLang,
          slf4j),
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
      })

  lazy val `sbt-karaf` = project
    .dependsOn(`sbt-karaf-packaging`)
    .settings(commonPluginSettings: _*)
    .settings(
      libraryDependencies ++= Karaf.common :+ commonsLang,
      unmanagedSourceDirectories in Compile ++= Seq(
        (sourceDirectory in Compile in `karaf-mbean-wrapper`).value))

  val commonSettings = Seq(
    organization in ThisBuild := "wav.devtools",
    version := "0.1.0.SNAPSHOT",
    externalResolvers ++= Seq(
      Resolver.sbtPluginRepo("releases"),
      Resolver.typesafeIvyRepo("releases")),
    publishArtifact in Compile := true,
    publishArtifact in Test := false,
    scalaVersion := "2.10.5",
    scalacOptions in ThisBuild ++= Seq(
      "-deprecation", 
      "-feature", 
      "-unchecked", 
      "-Xlint", 
      "-language:implicitConversions"),
    javacOptions in ThisBuild ++= Seq(
      "-source", "1.8",
      "-target", "1.8",
      "-encoding", "UTF8",
      "-Xlint:deprecation",
      "-Xlint:unchecked"))

  val commonPluginSettings =
    addMavenResolverPlugin ++
    commonSettings ++
    scriptedSettings ++
    Seq(
      sbtPlugin := true,
      scriptedLaunchOpts += "-Dproject.version=" + version.value,
      fork in scripted := true) // this may be required for log output

  val features = Seq(
    Karaf.standardFeatures,
    Karaf.enterpriseFeatures,
    Karaf.paxWebFeatures)

}