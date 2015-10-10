package wav.devtools.sbt.karaf

import sbt._

object Dependencies {

  val slf4j          = "org.slf4j" % "slf4j-api" % "1.7.12"
  val commonsLang    = "org.apache.commons" % "commons-lang3" % "3.4"
  val commonsIo      = "commons-io" % "commons-io" % "2.4"
  val osgiCore       = "org.osgi" % "org.osgi.core" % "6.0.0"
  val junit          = "junit" % "junit" % "4.11" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"
  val scalaTest      = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  val ariesJmx       = "org.apache.aries.jmx" % "org.apache.aries.jmx" % "1.1.5"
  val jarchivelib    = "org.rauschig" % "jarchivelib" % "0.7.1"

  object Karaf {

    val Version = "4.0.1"

    // when this is changed, update the the sbt-karaf-packaging tests
    lazy val testFeatures = Seq(
      featureID("org.apache.camel.karaf", "apache-camel", "2.16.0"),
      featureID("org.apache.karaf.features", "standard", Version),
      featureID("org.apache.karaf.features", "enterprise", Version),
      featureID("org.ops4j.pax.web", "pax-web-features", "4.2.0")).map(_ % "test")

    private def featureID(o: String, n: String, v: String, a: Option[String] = None) =
      ModuleID(o, n, v, isTransitive = false, explicitArtifacts = Seq(Artifact(a getOrElse n, "xml", "xml", "features")))

    val profile = "org.apache.karaf.profile" % "org.apache.karaf.profile.core" % Version

    lazy val jmxDependencies = Seq(
      Karaf.bundle,
      Karaf.config,
      Karaf.features,
      Karaf.system)

    // Karaf's MBean dependencies, see: http://karaf.apache.org/manual/latest/users-guide/monitoring.html

    // {{org.apache.karaf:type=config,name=*}}: management of the OSGi bundles.
    val config   = kmodule("config")
    // {{org.apache.karaf:type=bundle,name=*}}: management of the configurations.
    val bundle   = kmodule("bundle")
    // {{org.apache.karaf:type=feature,name=*}}: management of the Apache Karaf features.
    val features = kmodule("features")
    // {{org.apache.karaf:type=system,name=*}}: management of the Apache Karaf container.
    val system = kmodule("system")

    private def kmodule(module: String) =
      s"org.apache.karaf.$module" % s"org.apache.karaf.$module.core" % Version withSources() notTransitive()
  }

}