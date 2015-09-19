package wav.devtools.sbt.karaf.packaging.model

import org.scalatest.Spec
import wav.devtools.sbt.karaf.packaging.{Util => ThisUtil}

class UtilSuite extends Spec {

  def `can download mvn url`(): Unit = {
    val resolvers = Seq(sbt.Resolver.mavenLocal)
    sbt.IO.withTemporaryDirectory { cacheDir =>
      val result = ThisUtil.downloadMavenArtifact(sbt.uri("mvn:org.scala-lang/scala-library/2.11.7"), cacheDir, resolvers)
      val Some(f) = result
      f.length() > 1024 * 50 // more that 50KB.
    }
  }

}