import wav.devtools.sbt.karaf.{Properties => props, Dependencies => deps}

organization in ThisBuild := props.org

name := "osgitools"

version in ThisBuild := props.version

lazy val `sbt-karaf-packaging` = project
  .settings(addMavenResolverPlugin: _*)

lazy val `karaf-mbean-wrapper` = project
  .settings(
    libraryDependencies ++= deps.Karaf.common)

lazy val `sbt-karaf` = project
  .settings(
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile in `sbt-karaf-packaging`).value,
      (sourceDirectory in Compile in `karaf-mbean-wrapper`).value))

scalaVersion in ThisBuild := "2.11.7"

crossScalaVersions in ThisBuild := Seq("2.10.5", "2.11.7")

resolvers in ThisBuild ++= Seq(
  Resolver.sbtPluginRepo("releases"),
  Resolver.typesafeIvyRepo("releases"))

props.compilerOptions

publishArtifact in ThisBuild := false

updateOptions := updateOptions.value.withCachedResolution(true)