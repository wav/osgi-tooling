import wav.devtools.sbt.karaf.packaging.KarafPackagingKeys._
import wav.devtools.sbt.karaf.packaging.{SbtKarafPackaging, KarafPackagingDefaults}, SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

import com.typesafe.sbt.osgi.SbtOsgi, SbtOsgi.autoImport._

enablePlugins(SbtOsgi, SbtKarafPackaging)

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

OsgiKeys.importPackage := Seq(
	"org.osgi.framework",
	"scala",
	"scala.*")

/**
 * Inorder to get pax test to run, you only need:
 * - Pax dependencies for a container as per the pax exam website
 * - A JUnit test framework must be registered with SBT
 */

/**
 * Pax exam dependencies: // https://ops4j1.jira.com/wiki/display/PAXEXAM4/Karaf+Container
 */

resolvers in ThisBuild := Seq(
  Resolver.mavenLocal,
  DefaultMavenRepository)

val vPaxExam = "4.6.0"

libraryDependencies ++=
  Seq(
    "org.osgi" % "org.osgi.core" % "6.0.0" % "provided",
    FeatureID("org.apache.karaf.features", "standard", "4.0.1"),

    "org.ops4j.pax.exam" % "pax-exam" % vPaxExam % "test",
    "org.ops4j.pax.exam" % "pax-exam-container-karaf" % vPaxExam % "test",
    "org.ops4j.pax.url" % "pax-url-aether" % "1.6.0" % "test",
    "javax.inject" % "javax.inject" % "1" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "org.ops4j.pax.exam" % "pax-exam-junit4" % vPaxExam % "test")


shouldGenerateDependsFile := true

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

javaOptions +=
  s"-Dkaraf.distribution=${(target.value / karafDistribution.value.artifactName).toURI.toString}"

test in Test <<= (test in Test).dependsOn(karafSourceDistribution)

fork in Test := true  // IMPORTANT, forking ensures that the container starts with the correct classpath

logLevel := Level.Warn