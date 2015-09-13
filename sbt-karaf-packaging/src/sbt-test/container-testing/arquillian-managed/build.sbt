import wav.devtools.sbt.karaf.packaging.{SbtKarafPackaging, KarafPackagingDefaults}, SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

resolvers := Seq(
  Resolver.mavenLocal,
  DefaultMavenRepository)

libraryDependencies ++= Seq(
  "org.osgi" % "osgi.core" % "6.0.0" % "provided",
  "org.osgi" % "osgi.enterprise" % "6.0.0" % "provided",
  "org.apache.felix" % "org.apache.felix.framework" % "5.0.1" % "provided",
  "org.apache.felix" % "org.apache.felix.main" % "5.0.1" % "provided",
  "org.jboss" % "jboss-vfs" % "3.2.10.Final" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.jboss.arquillian.junit" % "arquillian-junit-container" % "1.1.2.Final" % "test",
  // managed not working, need to hack 2.1.0.CR19-SNAPSHOT
  "org.jboss.arquillian.container" % "arquillian-container-karaf-managed" % "2.1.0.CR18" % "test")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

shouldDownloadKarafDistribution

test in Test <<= (test in Test).dependsOn(unpackKarafDistribution)

//fork in Test := true