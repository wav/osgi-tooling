package wav.devtools.sbt.karaf.packaging

import sbt.{Artifact, ModuleID}

trait PluginSyntax {

    def FeatureID(o: String, n: String, v: String, a: Option[String] = None) =
      ModuleID(o, n, v, explicitArtifacts = Seq(Artifact(a getOrElse s"$n", "xml", "xml", "features")))

}
