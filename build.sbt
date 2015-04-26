import wav.devtools.sbt.karaf.{Properties => props}

organization in ThisBuild := props.org

name := "osgitools"

version in ThisBuild := props.version

lazy val util = project

lazy val `karaf-management` = project

lazy val `sbt-karaf` = project
	.settings(
		unmanagedSourceDirectories in Compile +=
        	(sourceDirectory in Compile in `util`).value,
		unmanagedSourceDirectories in Compile +=
        	(sourceDirectory in Compile in `karaf-management`).value)

scalaVersion in ThisBuild := "2.11.6"

crossScalaVersions in ThisBuild := Seq("2.10.5", "2.11.6")

resolvers in ThisBuild ++= Seq(
  Resolver.sbtPluginRepo("releases"),
  Resolver.typesafeIvyRepo("releases"))

props.compilerOptions

publishArtifact in ThisBuild := false