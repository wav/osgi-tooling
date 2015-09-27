package wav.devtools.sbt.karaf.packaging

import sbt.{Artifact, ModuleID}

trait PluginSyntax {

  def FeatureID(o: String, n: String, v: String, a: Option[String] = None): ModuleID =
    ModuleID(o, n, v, explicitArtifacts = Seq(Artifact(a getOrElse n, "xml", "xml", "features")))

  implicit class RichModuleID(m: sbt.ModuleID) {
    def toWrappedBundle(instructions: Map[String, String] = Map.empty, instructionsUrl: Option[String] = None): ModuleID = {
      require(
        instructions.nonEmpty || instructionsUrl.nonEmpty,
        s"$m wrapBundle must have instructions set")
      val FeaturesXml.WrappedBundlePattern(url,instUrl,insts) =
        FeaturesXml.WrappedBundle("scheme:NOT_SET", instructions, instructionsUrl).url
      val attrValue = nullToEmpty(instUrl) + nullToEmpty(insts)
      m.copy(extraAttributes = m.extraAttributes + (SbtResolution.WRAP_BUNDLE_INSTRUCTIONS -> attrValue))
    }
  }

  val FeaturesXml = wav.devtools.karaf.packaging.FeaturesXml

  val MavenUrl = wav.devtools.karaf.packaging.MavenUrl

}