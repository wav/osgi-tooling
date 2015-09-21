package wav.devtools.sbt.karaf.packaging.model

import org.scalatest.Spec
import java.io.File
import wav.devtools.sbt.karaf.packaging.{Util => ThisUtil}
import sbt._

class UtilSuite extends Spec {

  val javaxInject = "mvn:javax.inject/javax.inject/1"
  val javaxInjectPath = org.apache.karaf.util.maven.Parser.pathFromMaven(javaxInject)
  val resolvers = Seq(
    sbt.Resolver.mavenLocal,
    sbt.Resolver.jcenterRepo,
    "central" at sbt.Resolver.DefaultMavenRepositoryRoot)

  def `can download mvn url`(): Unit =
    sbt.IO.withTemporaryDirectory { cacheDir =>
      val result = ThisUtil.downloadMavenArtifact(sbt.uri(javaxInject), cacheDir, resolvers)
      val Some(f) = result
      f.length() > 1024 * 50 // more than 50KB.
    }

  def `can download a cached url`(): Unit = {
    sbt.IO.withTemporaryDirectory { cacheDir =>
      val result1 = ThisUtil.downloadMavenArtifact(sbt.uri(javaxInject), cacheDir, resolvers)
      val Some(f1) = result1
      f1.length() > 1024 * 50 // more than 50KB.
      val result2 = ThisUtil.downloadMavenArtifact(sbt.uri(javaxInject), cacheDir, Seq.empty)
      val Some(f2) = result2
      f2.length() > 1024 * 50 // more than 50KB.
    }
  }

}