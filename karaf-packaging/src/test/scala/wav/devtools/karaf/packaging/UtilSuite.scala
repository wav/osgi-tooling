package wav.devtools.karaf.packaging

import org.scalatest.Spec
import java.net.URI

class UtilSuite extends Spec {

  val javaxInject = "mvn:javax.inject/javax.inject/1"
  val javaxInjectPath = org.apache.karaf.util.maven.Parser.pathFromMaven(javaxInject)
  val resolvers = Seq(
    localRepo,
    jcenterRepo,
    centralRepo)

  def `can download mvn url`(): Unit =
    Util.withTemporaryDirectory { cacheDir =>
      val result = Util.downloadMavenArtifact(new URI(javaxInject), cacheDir, resolvers)
      val Some(f) = result
      f.length() > 1024 * 50 // more than 50KB.
    }

  def `can download a cached url`(): Unit = {
    Util.withTemporaryDirectory { cacheDir =>
      val result1 = Util.downloadMavenArtifact(new URI(javaxInject), cacheDir, resolvers)
      val Some(f1) = result1
      f1.length() > 1024 * 50 // more than 50KB.
      val result2 = Util.downloadMavenArtifact(new URI(javaxInject), cacheDir, Seq.empty)
      val Some(f2) = result2
      f2.length() > 1024 * 50 // more than 50KB.
    }
  }

}