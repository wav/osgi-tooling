package wav.devtools.sbt.karaf.packaging.model

import org.scalatest.Spec
import java.io.File
import wav.devtools.sbt.karaf.packaging.{Util => ThisUtil}
import sbt._

class UtilSuite extends Spec {

  val javaxInject = "mvn:javax.inject/javax.inject/1"
  val javaxInjectPath = org.apache.karaf.util.maven.Parser.pathFromMaven(javaxInject)

  def `can download mvn url`(): Unit = {
    val resolvers = Seq(sbt.Resolver.mavenLocal, "central" at sbt.Resolver.DefaultMavenRepositoryRoot)
    sbt.IO.withTemporaryDirectory { cacheDir =>
      val existingFile = Some(new File(cacheDir, javaxInjectPath))
        .foreach(_.delete())
      val result = ThisUtil.downloadMavenArtifact(sbt.uri(javaxInject), cacheDir, resolvers)
      val Some(f) = result
      f.length() > 1024 * 50 // more that 50KB.
    }
  }

  def `can download a cached url`(): Unit = {
    val resolvers1 = Seq(sbt.Resolver.mavenLocal, "central" at sbt.Resolver.DefaultMavenRepositoryRoot)
    val resolvers2 = Seq(sbt.Resolver.mavenLocal)
    sbt.IO.withTemporaryDirectory { cacheDir =>
      val result1 = ThisUtil.downloadMavenArtifact(sbt.uri(javaxInject), cacheDir, resolvers1)
      val result2 = ThisUtil.downloadMavenArtifact(sbt.uri(javaxInject), cacheDir, resolvers2)
      val Some(f) = result2
      f.length() > 1024 * 50 // more that 50KB.
    }
  }

}