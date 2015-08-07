package wav.devtools.sbt.karaf.packaging

import sbt._
import sbt.Keys._
import wav.devtools.sbt.karaf.packaging.model.FeaturesArtifact

private[packaging] object Resolution {

  val featuresArtifactFilter = artifactFilter(name = "*", `type` = "xml", extension = "xml", classifier = "features")

  val bundleArtifactFilter = artifactFilter(name = "*", `type` = "jar" | "bundle", extension = "jar", classifier = "*")

  def toFeaturesArtifacts(mr: ModuleReport): Seq[FeaturesArtifact] =
    for {(a, f) <- mr.artifacts} yield
    FeaturesArtifact(mr.module, a, f)

  def resolveArtifacts(ur: UpdateReport): SbtTask[Seq[FeaturesArtifact]] = Def.task {
    val filtered = ur.filter(featuresArtifactFilter | bundleArtifactFilter)
    ur.allConfigurations.flatMap(ur.configuration(_).get.modules.flatMap(toFeaturesArtifacts))
  }

  // How do we resolve ?!? .. maybe ...
  // transform the sbt `updateClassifiers` task for adding undefined modules then use the `update` task to collecting results and build a features file.

  // resolution strategy.
  //     1. Resolve all repositories
  //     2. Resolve all features
  //     3. Add all bundles found in features

  // Nice to haves.
  //    a. identify artifacts that come from resolvers that are of the maven type and use MavenUrl

}