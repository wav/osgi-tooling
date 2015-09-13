package wav.devtools.sbt.karaf.packaging

import sbt.Keys._
import sbt._
import wav.devtools.sbt.karaf.packaging
import packaging.model._, FeaturesXml._

object KarafPackagingDefaults {

  import KarafPackagingKeys._

  lazy val featuresXmlTask = Def.task {
    new FeaturesXml(name.value, featuresRepositories.value.toSeq :+ featuresProjectFeature.value)
  }

  lazy val featuresFileTask = Def.task {
    Util.write(
      crossTarget.value / "features.xml",
      FeaturesXmlFormats.featuresXsd,
      FeaturesXmlFormats.makeFeaturesXml(featuresXml.value))
  }

  private lazy val allFeaturesRepositories   = taskKey[Set[FeatureRepository]]("All resolved features repositories")

  private lazy val allFeaturesRepositoriesTask = Def.task {
    val resolveAll = Resolution.resolveAllFeatureRepositoriesTask.value
    resolveAll(update.value)
  }

  lazy val featuresRepositoriesTask = Def.task {
    val constraints = featuresRequired.value.map(toDep).toSet
    val repos = allFeaturesRepositories.value
    for {
      fr <- repos
      f <- fr.features
      c <- constraints
      if (Resolution.satisfies(c,f))
    } yield Repository(fr.url)
  }

  lazy val featuresSelectedTask = Def.task {
    val constraints = featuresRequired.value.map(toDep).toSet
    val repos = allFeaturesRepositories.value
    Resolution.resolveRequiredFeatures(constraints, repos)
  }

  lazy val featuresProjectBundleTask = Def.task {
    val (_, f) = (packagedArtifact in(Compile, packageBin)).value
    Bundle(f.toURI.toString)
  }

  lazy val featuresProjectFeatureTask = Def.task {
    val features = featuresRequired.value.map(toDep)
    val selected = featuresSelected.value
    val resolved = Resolution.mustResolveFeatures(selected)
    val bundles = Resolution.selectProjectBundles(update.value, resolved) + featuresProjectBundle.value
    feature(name.value, version.value, bundles ++ features)
  }

  lazy val generateDependsFileTask: SbtTask[Seq[File]] = Def.task {
    if (shouldGenerateDependsFile.value) {
      val f = (resourceManaged in Compile).value / packaging.model.DependenciesProperties.jarPath
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
      Seq(f)
    } else Seq.empty
  }

  lazy val featuresPackagedArtifactsTask: SbtTask[Map[Artifact, File]] = Def.task {
    val pas = packagedArtifacts.value
    featuresFile.value
      .map(f => pas.updated(Artifact(name.value, "xml", "xml", "features"), f))
      .getOrElse(pas)
  }

  lazy val downloadKarafDistributionTask: SbtTask[File] = Def.task {
    val source = karafDistribution.value
    val archive = karafSourceDistribution.value
    if (!archive.exists) {
      val rs = resolvers.value.collect { case r: MavenRepository => r }.sortBy(_.isCache)
      val result = Util.download(source.uri, { temp => IO.copyFile(temp, archive); archive }, rs)
      require(result.isDefined, s"Couldn't download $source")
    }
    archive
  }

  lazy val unpackKarafDistributionTask: SbtTask[File] = Def.task {
    val source = karafDistribution.value
    val archive: File = {
      val f: File = karafSourceDistribution.value
      if (f.exists()) f
      else downloadKarafDistribution.value.getOrElse(f)
    }
    val karafDist = target.value / "karaf-dist"
    Util.unpack(archive, karafDist)
    val contentPath = Option(source.contentPath).filterNot(_.isEmpty).map(karafDist / _)
    val finalKarafDist = contentPath getOrElse karafDist
    require(finalKarafDist.isDirectory(), s"$finalKarafDist not found")
    finalKarafDist
  }

  val KarafMinimalDistribution =
    KarafDistribution(
      uri(s"mvn:org.apache.karaf/apache-karaf-minimal/4.0.1/tar.gz"),
      s"apache-karaf-minimal-4.0.1")

  lazy val karafDistributionSettings: Seq[Setting[_]] =
    Seq(
      libraryDependencies += "org.apache.karaf" % "apache-karaf-minimal" % "4.0.1" from(karafSourceDistribution.value.toURI.toString),
      update <<= update.dependsOn(downloadKarafDistribution),
      karafDistribution := KarafMinimalDistribution,
      karafSourceDistribution := target.value / s"apache-karaf-minimal-4.0.1.tar.gz",
      unpackKarafDistribution := unpackKarafDistributionTask.value,
      downloadKarafDistribution := None)

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
        packagedArtifacts <<= featuresPackagedArtifactsTask,
        featuresAddDependencies := false,
        shouldGenerateDependsFile := false,
        resourceGenerators in Compile <+= generateDependsFileTask)

}