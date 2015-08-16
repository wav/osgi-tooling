package wav.devtools.sbt.karaf.packaging

import sbt.Keys._
import sbt._
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml.{Bundle, Feature, FeatureRef}
import wav.devtools.sbt.karaf.packaging.model.{FeaturesArtifact, FeaturesRepository, MavenUrl}

import scala.collection.mutable

private[packaging] object Resolution {
  import model.FeaturesArtifactData.canBeFeaturesRepository

  val featuresArtifactFilter = artifactFilter(name = "*", `type` = "xml", extension = "xml", classifier = "features")

  val bundleArtifactFilter = artifactFilter(name = "*", `type` = "jar" | "bundle", extension = "*", classifier = "*")

  def emptyReport(module: ModuleID): ModuleReport =
    ModuleReport(module, (module.explicitArtifacts.map((_, null))), Nil)

  def resolveFeaturesRepository(logger: Logger, mr: ModuleReport): Either[String, Seq[FeaturesRepository]] = {
    val fas = for {
      (a, f) <- mr.artifacts
      if (canBeFeaturesRepository(a))
    } yield FeaturesArtifact(mr.module, a, Some(f), None)
    val notDownloaded = fas.filterNot(_.downloaded)
    if (notDownloaded.nonEmpty) Left(s"Failed to resolve all the features repositories for the module: ${mr.module}, missing artifact: ${notDownloaded.map(_.artifact.name)}")
    else Right(fas.flatMap { fa =>
      val r = fa.toRepository
      if (r.isEmpty) logger.warn(s"Ignored possible features repository, content not known: Artifact ${fa.artifact}, Module ${mr.module}")
      r
    })
  }

  def resolveAllFeaturesRepositoriesTask: SbtTask[UpdateReport => Set[FeaturesRepository]] = Def.task {
    val logger = streams.value.log
    ur => {
      val fas = ur.filter(featuresArtifactFilter)
      val results = fas.configurations.flatMap(_.modules).map(resolveFeaturesRepository(logger, _))
      val failures = results.collect { case Left(e) => e }
      failures.foreach(logger.error(_))
      if (failures.nonEmpty)
        sys.error("Could not resolve all features repositories.")
      results.collect { case Right(frs) => frs }.flatten.toSet
    }
  }

  def toRefs(m: Map[String, String]): Set[FeatureRef] = {
    m.map(e => FeatureRef(e._1, {
      require(e._2 != null && e._2.trim.length > 0, s"The referenced feature ${e._1} does not have a valid version identifier, use `*` to specify any version.")
      if (e._2 == "*") None else Some(e._2)
    })).toSet
  }

  def selectNewest(requested: Set[FeatureRef], collection: Set[FeatureRef]): Set[FeatureRef] = {
    val selection = mutable.Set[FeatureRef]()
    for (ref <- requested) {
      val matches = collection.filter(_.name == ref.name).toArray.sorted.toSeq
      if (ref.version.isDefined && matches.contains(ref)) selection += ref
      else if (matches.nonEmpty) selection += matches.last
    }
    selection.toSet
  }

  def requireAllFeatures(required: Map[String, String], repositories: Set[FeaturesRepository]): Unit = {
    val available = repositories.flatMap(_.features)
    val requiredRefs = toRefs(required)

    val selected = selectNewest(requiredRefs, available)
    val missing = requiredRefs.map(_.name) -- selected.map(_.name)

    var reportItems = Seq.empty[String]
    if (missing.nonEmpty) reportItems = reportItems :+ s"missing dependencies: ${missing.mkString(",")}"

    // TODO: recurse.
    val transitive = selectTransitiveDependencies(requiredRefs, repositories)
    val selectedTransitive = selectNewest(transitive, available)
    val transitiveMissing = transitive.map(_.name) -- selectedTransitive.map(_.name)
    if (transitiveMissing.nonEmpty) reportItems = reportItems :+ s"missing transitive dependencies: ${transitiveMissing.mkString(",")}"

    if (reportItems.nonEmpty)
      sys.error(
        "The feature repositories defined in the build do not contain all required features. You may need to add more feature repositories to your build." +
          reportItems.mkString("\n| - ", "\n| - ", "").stripMargin)
  }

  def selectTransitiveDependencies(required: Set[FeatureRef], repositories: Set[FeaturesRepository]): Set[FeatureRef] =
    for {
      fr <- repositories
      dep <- selectNewest(required, fr.features) // TODO: review
      f <- fr.featuresXml.elems.collect { case f: Feature => f }
      if (f.toRef.name == dep.name)
      tranDep <- f.deps.collect { case ref: FeatureRef => ref }
    } yield tranDep

  def filterFeatureRepositories(required: Map[String, String], repositories: Set[FeaturesRepository]): Set[FeaturesRepository] = {
    val available = repositories.flatMap(_.features)
    val requiredRefs = toRefs(required)
    val selected = selectNewest(requiredRefs, available)
    val selectedTransitive = selectNewest(selectTransitiveDependencies(requiredRefs, repositories), available)
    val all = selected ++ selectedTransitive

    repositories.map { fr =>
      val filtered = fr.featuresXml.elems.flatMap {
        case f: Feature => if (all.map(_.name).contains(f.toRef.name)) Some(f) else None
        case e => Some(e)
      }
      val newXml = fr.featuresXml.copy(elems = filtered)
      new FeaturesRepository(fr.module, fr.artifact, fr.file, newXml)
    }
  }

  // INCOMPLETE
  def selectProjectBundles(ur: UpdateReport, repositories: Set[FeaturesRepository]): Set[Bundle] = {
    val cr = ur.filter(bundleArtifactFilter).configuration("runtime").get
    val mavenUrls = (for {
      mr <- cr.modules
      m = mr.module
      (a, _) <- mr.artifacts
      fr <- repositories
      b <- fr.dependencies.collect { case Bundle(url) => MavenUrl.unapply(url) }.flatten
      if (b.groupId == m.organization && 
          b.artifactId == a.name && 
          b.version == m.revision &&
          b.`type` == a.`type` &&
          b.classifer == a.classifier) // TODO: type
    } yield b).toSet

    val allAsMavenUrls = (for {
      mr <- cr.modules
      m = mr.module
      (a, _) <- mr.artifacts
    } yield MavenUrl(m.organization, a.name, m.revision, Option(a.`type`), a.classifier)).toSet

    (allAsMavenUrls -- mavenUrls).map(url => Bundle(url.toString))
  }

}