package wav.devtools.sbt.karaf.packaging

import sbt.Keys._
import sbt._
import wav.devtools.sbt.karaf.packaging.KarafPackagingKeys._
import wav.devtools.sbt.karaf.packaging.model.{MavenUrl, FeatureRepository}

private [packaging] object Internal {

  lazy val addDependenciesInFeaturesRepositories: State => State =
    state => {
      val extracted = Project.extract(state)
      val shouldAdd = extracted.get(featuresAddDependencies)
      if (shouldAdd) {
        val constraints = extracted.get(featuresRequired).map(toRef).toSet
        val (_, repos) = extracted.runTask(downloadAllFeatureRepositories, state)
        val result = Resolution.resolveRequiredFeatures(constraints, repos)
        val resolved = Resolution.mustResolveFeatures(result)
        val newDependencies = Resolution.toLibraryDependencies(resolved)
        extracted.append(Seq(
          libraryDependencies ++= newDependencies,
          featuresAddDependencies := false
        ), state)
      } else state
    }

  lazy val downloadAllFeatureRepositories = taskKey[Set[FeatureRepository]]("download all features repositories")

  lazy val downloadAllFeatureRepositoriesTask: SbtTask[Set[FeatureRepository]] = Def.task {
    val logger = streams.value.log
    val download = (url: MavenUrl) => Ivy.
      downloadMavenArtifact(url, externalResolvers.value, ivySbt.value, logger, updateOptions.value)
    val resolve = (m: ModuleID) => Resolution.downloadFeaturesRepository(logger, download, m)
    val results = libraryDependencies.value.map(resolve)
    val failures = results.collect { case Left(e) => e }
    failures.foreach(logger.error(_))
    if (failures.nonEmpty)
      sys.error("Could not resolve all features repositories.")
    results.collect { case Right(frs) => frs }.flatten.toSet
  }

  lazy val settings: Seq[Setting[_]] = Seq(
    downloadAllFeatureRepositories := downloadAllFeatureRepositoriesTask.value)

}