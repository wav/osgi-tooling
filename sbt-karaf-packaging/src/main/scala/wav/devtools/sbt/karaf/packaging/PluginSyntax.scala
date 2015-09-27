package wav.devtools.sbt.karaf.packaging

import sbt.{Artifact, ModuleID}

trait PluginSyntax {

  def FeatureID(o: String, n: String, v: String, a: Option[String] = None) =
    ModuleID(o, n, v, explicitArtifacts = Seq(Artifact(a getOrElse n, "xml", "xml", "features")))

  val FeaturesXml = wav.devtools.karaf.packaging.FeaturesXml

  val MavenUrl = wav.devtools.karaf.packaging.MavenUrl

}