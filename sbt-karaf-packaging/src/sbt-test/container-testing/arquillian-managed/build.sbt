version := "0.1.0.SNAPSHOT"

scalaVersion := "2.11.7"

resolvers := Seq(
  Resolver.mavenLocal,
  DefaultMavenRepository)

val vKaraf = "4.0.1"

val assemblyPath = Resolver.publishMavenLocal.rootFile / "org/apache/karaf" / "apache-karaf-minimal" / vKaraf / s"apache-karaf-minimal-$vKaraf.tar.gz"

val assembly = "org.apache.karaf" % "apache-karaf-minimal" % vKaraf from(assemblyPath.toURI.toString)

lazy val prepareDistribution = taskKey[File]("prepare the karaf distribution for testing")

prepareDistribution := {
  import org.rauschig.jarchivelib._
  val distDir = target.value / s"apache-karaf-minimal-$vKaraf"
  val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
  archiver.extract(assemblyPath, target.value);
  distDir
}

libraryDependencies ++= Seq(
  assembly,
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

test in Test <<= (test in Test).dependsOn(prepareDistribution)

//fork in Test := true