package wav.devtools.sbt.karaf

import sbt._
import Keys._

object Properties {

    val org = "wav.devtools"
    val version = "0.1-SNAPSHOT"

    val compilerOptions = Seq(
        scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-language:implicitConversions"),
        javacOptions in ThisBuild ++= Seq(
            "-source",    "1.8",
            "-target",    "1.8",
            "-encoding",  "UTF8",
            "-Xlint:deprecation",
            "-Xlint:unchecked"))

    val ivyLocal = Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

    val pluginSettings = compilerOptions ++ Seq(
        libraryDependencies += Dependencies.`slf4j-simple`,
        externalResolvers := Seq(ivyLocal),
        addSbtPlugin(org % "sbt-karaf" % version))

    val packagingPluginSettings = compilerOptions ++ Seq(
      libraryDependencies += Dependencies.`slf4j-simple`,
      externalResolvers := Seq(ivyLocal),
      addSbtPlugin(org % "sbt-karaf-packaging" % version))

}

object Dependencies {

  val slf4j                   = "org.slf4j" % "slf4j-api" % "1.7.10"
  val `slf4j-simple`          = "org.slf4j" % "slf4j-simple" % "1.7.10"
  val commonsLang             = "org.apache.commons" % "commons-lang3" % "3.4"
  val osgiCore                = "org.osgi" % "org.osgi.core" % "6.0.0"
  val scalaTest               = "org.scalatest" %% "scalatest" % "2.2.4" % "test"

  /**
   * Pax exam dependencies: // https://ops4j1.jira.com/wiki/display/PAXEXAM4/Karaf+Container
   */
  val vPaxExam = "4.5.0"
  val paxExam = "org.ops4j.pax.exam" % "pax-exam" % vPaxExam % "test"
  val paxKaraf = "org.ops4j.pax.exam" % "pax-exam-container-karaf" % vPaxExam % "test"
  val paxAether = "org.ops4j.pax.url" % "pax-url-aether" % "1.6.0" % "test"
  val javaxInject = "javax.inject" % "javax.inject" % "1" % "test"
  val paxJunit = "org.ops4j.pax.exam" % "pax-exam-junit4" % vPaxExam % "test"
  val junit = "junit" % "junit" % "4.11" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"

  object Karaf {

    val Version = "4.0.0"

    val `package` = (("org.apache.karaf" % "apache-karaf" % Version)
      .artifacts(Artifact("apache-karaf", `type` = "tar.gz", extension = "tar.gz"))
      .intransitive)

    lazy val common = Seq(
      slf4j,
      osgiCore,
      Karaf.bundle,
      Karaf.config,
      Karaf.features,
      Karaf.instance)

    // Karaf's MBean dependencies, see: http://karaf.apache.org/manual/latest/users-guide/monitoring.html
    val config = kmodule("config") // {{org.apache.karaf:type=config,name=*}}: management of the OSGi bundles.
    val bundle = kmodule("bundle") // {{org.apache.karaf:type=bundle,name=*}}: management of the configurations.
    val features = kmodule("features") // {{org.apache.karaf:type=feature,name=*}}: management of the Apache Karaf features.
    val instance = kmodule("instance") // {{org.apache.karaf:type=instance,name=*}}: management of the instances.

    private def kmodule(module: String) =
        s"org.apache.karaf.$module" % s"org.apache.karaf.$module.core" % Version withSources() notTransitive()
  }

}