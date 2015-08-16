package wav.devtools.sbt.karaf.packaging

object FeaturesRepositoryID {
  def Artifact(name: String): sbt.Artifact =
    sbt.Artifact(name, `type` = "xml", extension = "xml", classifier = "features")

  implicit def featuresRepositoryIDModuleID(id: FeaturesRepositoryID): sbt.ModuleID =
    id.module

}

final case class FeaturesRepositoryID(organization: String, name: String, revision: String, repositories: Seq[String] = Nil) {
  import FeaturesRepositoryID._

  lazy val module: sbt.ModuleID = {
    val artifacts =
      if (repositories.isEmpty) Seq(Artifact(name)) else repositories.map(Artifact(_))
    sbt.ModuleID(organization, name, revision, isTransitive = false, explicitArtifacts = artifacts)
  }

}