package wav.devtools.sbt.karaf.packaging

import sbt.Keys._
import sbt._
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._
import wav.devtools.sbt.karaf.packaging.model._

object KarafPackagingDefaults {

  import KarafPackagingKeys._

  lazy val featuresXmlTask = Def.task {
    new FeaturesXml(name.value, featuresRepositories.value.toSeq :+ featuresProjectFeature.value)
  }

  lazy val featuresFileTask = Def.task {
    Util.write(
      crossTarget.value / "features.xml",
      featuresXsd,
      makeFeaturesXml(featuresXml.value))
  }

  private lazy val allFeaturesRepositories   = taskKey[Set[FeatureRepository]]("All resolved features repositories")

  private lazy val allFeaturesRepositoriesTask = Def.task {
    val resolveAll = Resolution.resolveAllFeatureRepositoriesTask.value
    resolveAll(update.value)
  }

  lazy val featuresRepositoriesTask = Def.task {
    val constraints = featuresRequired.value.map(toRef).toSet
    val repos = allFeaturesRepositories.value
    for {
      fr <- repos
      f <- fr.features
      c <- constraints
      if (Resolution.satisfies(c,f))
    } yield Repository(fr.url)
  }

  lazy val featuresSelectedTask = Def.task {
    val constraints = featuresRequired.value.map(toRef).toSet
    val repos = allFeaturesRepositories.value
    Resolution.resolveRequiredFeatures(constraints, repos)
  }

  lazy val featuresProjectBundleTask = Def.task {
    val (_, f) = (packagedArtifact in(Compile, packageBin)).value
    Bundle(f.toURI.toString)
  }

  lazy val featuresProjectFeatureTask = Def.task {
    val features = featuresRequired.value.map(toRef)
    val selected = featuresSelected.value
    val resolved = Resolution.mustResolveFeatures(selected)
    val bundles = Resolution.selectProjectBundles(update.value, resolved) + featuresProjectBundle.value
    feature(name.value, version.value, bundles ++ features)
  }

  lazy val generateDependsFileTask = Def.task {
    val f = target.value / "dependencies.properties"
    val artifacts = for {
      conf <- update.value.configurations
      moduleReport <- conf.modules
      (a, _) <- moduleReport.artifacts
    } yield {
        val m = moduleReport.module
        DependenciesProperties.Artifact(m.organization, a.name, m.revision, conf.configuration, a.`type`)
      }
    val fcontent = DependenciesProperties(
      DependenciesProperties.Project(organization.value, name.value, version.value),
      artifacts)
    IO.write(f, fcontent)
    f
  }

  lazy val featuresPackagedArtifactsTask: SbtTask[Map[
    Artifact, File]] = Def.task {
    val pas = packagedArtifacts.value
    featuresFile.value
      .map(f => pas.updated(Artifact(name.value, "xml", "xml", "features"), f))
      .getOrElse(pas)
  }

  lazy val featuresSettings: Seq[Setting[_]] =
      Internal.settings ++
      Seq(
        featuresXml := featuresXmlTask.value,
        featuresFile := Some(featuresFileTask.value),
        featuresRequired := Map.empty,
        featuresRepositories := featuresRepositoriesTask.value,
        allFeaturesRepositories := allFeaturesRepositoriesTask.value,
        featuresSelected := featuresSelectedTask.value,
        featuresProjectBundle := featuresProjectBundleTask.value,
        featuresProjectFeature := featuresProjectFeatureTask.value,
        generateDependsFile := generateDependsFileTask.value,
        packagedArtifacts <<= featuresPackagedArtifactsTask,
        featuresAddDependencies := false)

}