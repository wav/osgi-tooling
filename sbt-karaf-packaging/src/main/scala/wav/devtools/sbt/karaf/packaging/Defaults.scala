package wav.devtools.sbt.karaf.packaging

import sbt.Keys._
import sbt._
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._
import wav.devtools.sbt.karaf.packaging.model.{DependenciesProperties, FeaturesXml}

object KarafPackagingDefaults extends Import {

  lazy val featuresXmlTask = Def.task {
    new FeaturesXml(name.value, Some(version.value), Seq(featuresProjectFeature.value))
  }

  lazy val generateFeaturesFileTask = Def.task {
    val featuresTarget = crossTarget.value / "features.xml"
    val featuresSource = (resourceDirectory in Compile).value / "features.xml"
    Util.write(
      featuresTarget,
      featuresSource,
      featuresXsd,
      Map.empty,
      makeFeaturesXml(featuresXml.value))
  }

  lazy val featuresRepositoriesTask = Def.task {
    val resolveAll = Resolution.resolveAllFeaturesRepositoriesTask.value
    resolveAll(update.value)
  }

  lazy val featuresSelectedTask = Def.task {
    val repos = featuresRepositories.value
    val feats = featuresRequired.value
    Resolution.requireAllFeatures(feats, repos)
    val filtered = Resolution.filterFeatureRepositories(feats, repos)
    filtered.flatMap(_.featuresXml.elems collect { case f: Feature => f })
  }

  lazy val featuresProjectBundleTask = Def.task {
    val (_, f) = (packagedArtifact in(Compile, packageBin)).value
    Bundle(f.toURI.toString)
  }

  lazy val featuresProjectFeatureTask = Def.task {
    val feats = featuresRequired.value
    val repos = featuresRepositories.value
    val filtered = Resolution.filterFeatureRepositories(feats, repos)
    val bundles = Resolution.selectProjectBundles(update.value, filtered)
    val deps = Set.empty[FeatureDependency] + 
      featuresProjectBundle.value ++ 
      bundles ++ 
      Resolution.toRefs(feats)
    Feature(name.value, Some(version.value), deps)
  }.dependsOn(featuresSelected)

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
    generateFeaturesFile.value
      .map(f => pas.updated(FeaturesRepositoryID.Artifact(name.value), f))
      .getOrElse(pas)
  }

  lazy val featuresSettings: Seq[Setting[_]] = Seq(
    featuresXml := featuresXmlTask.value,
    generateFeaturesFile := Some(generateFeaturesFileTask.value),
    featuresRequired := Map.empty,
    featuresRepositories := featuresRepositoriesTask.value,
    featuresSelected := featuresSelectedTask.value,
    featuresProjectBundle := featuresProjectBundleTask.value,
    featuresProjectFeature := featuresProjectFeatureTask.value,
    generateDependsFile := generateDependsFileTask.value,
    packagedArtifacts <<= featuresPackagedArtifactsTask)

}
