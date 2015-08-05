package wav.devtools.sbt.karaf.packaging

import sbt._
import sbt.Keys._
import wav.devtools.sbt.karaf.packaging.model.{FeaturesXml, FeaturesArtifact}

private[packaging] object Resolution {

  val featuresArtifactFilter = artifactFilter(name = "*", `type` = "xml", extension = "xml", classifier = "features")

  val bundleArtifactFilter = artifactFilter(name = "*", `type` = "jar" | "bundle", extension = "jar", classifier = "*")

  def toFeaturesArtifacts(mr: ModuleReport): Seq[FeaturesArtifact] =
    for {(a, f) <- mr.artifacts} yield
    FeaturesArtifact(f, a.url.map(_.toString), mr.module.organization, a.name, mr.module.revision, a.`type`, a.extension, a.classifier)

  def resolveArtifacts(config: Configuration, af: ArtifactFilter): SbtTask[Seq[FeaturesArtifact]] = Def.task {
    val confReport = (update in config).value.filter(af).configuration(config.name)
    confReport.map(_.modules.flatMap(toFeaturesArtifacts)) getOrElse Seq()
  }

  def resolveBundles(config: Configuration): SbtTask[Seq[FeaturesXml.ABundle]] =
    resolveArtifacts(config, bundleArtifactFilter).map(_.flatMap(FeaturesArtifact.toBundle))

  def resolveRepositories(config: Configuration): SbtTask[Seq[FeaturesXml.Repository]] =
    resolveArtifacts(config, featuresArtifactFilter).map(_.flatMap(FeaturesArtifact.toRepository))

}