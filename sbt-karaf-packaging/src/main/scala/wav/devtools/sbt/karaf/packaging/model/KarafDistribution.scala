package wav.devtools.sbt.karaf.packaging.model

import sbt._

case class KarafDistribution(url: URI, artifactName: String, contentPath: String) {
  override def toString(): String =
    s"KarafDistribution($url,$artifactName,$contentPath)"
}