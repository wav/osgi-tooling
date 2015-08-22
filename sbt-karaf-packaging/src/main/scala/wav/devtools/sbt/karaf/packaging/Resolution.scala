package wav.devtools.sbt.karaf.packaging

import org.osgi.framework.Version
import sbt.Keys._
import sbt._
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml.{Bundle, Feature, FeatureRef}
import wav.devtools.sbt.karaf.packaging.model.{FeaturesArtifact, FeatureRepository, MavenUrl}

import scala.annotation.tailrec

private[packaging] object Resolution {

  import model.FeaturesArtifactData.canBeFeaturesRepository

  val featuresArtifactFilter = artifactFilter(name = "*", `type` = "xml", extension = "xml", classifier = "features")

  val bundleArtifactFilter = artifactFilter(name = "*", `type` = "jar" | "bundle", extension = "*", classifier = "*")

  def emptyReport(module: ModuleID): ModuleReport =
    ModuleReport(module, (module.explicitArtifacts.map((_, null))), Nil)

  def resolveFeaturesRepository(logger: Logger, mr: ModuleReport): Either[String, Seq[FeatureRepository]] = {
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

  def resolveAllFeatureRepositoriesTask: SbtTask[UpdateReport => Set[FeatureRepository]] = Def.task {
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

  def resolveRequiredFeatures(required: Set[FeatureRef], repositories: Set[FeatureRepository]): Either[Set[FeatureRef], Set[Feature]] = {
    val allFeatures = for {
      fr <- repositories
      f <- fr.featuresXml.elems.collect { case f: Feature => f }
    } yield f
    resolveFeatures(required, allFeatures)
  }

  private val excludeBundleTypes = Set("bundle", "jar")
  def toBundle(m: ModuleID, a: Artifact) = {
    val t = Some(a.`type`).filterNot(excludeBundleTypes.contains)
    Bundle(MavenUrl(m.organization, m.name, m.revision, t, a.classifier).toString)
  }

  def selectProjectBundles(ur: UpdateReport, features: Set[Feature]): Set[Bundle] = {
    val mavenUrls = features
      .flatMap(_.bundles)
      .collect { case Bundle(MavenUrl(url)) => url }
    val cr = ur.filter(bundleArtifactFilter).configuration("runtime").get
    val inFeatures =
      for {
        mr <- cr.modules
        m = mr.module
        (a, _) <- mr.artifacts
        url <- mavenUrls
        if (url.groupId == m.organization && url.artifactId == m.name)
      } yield (m, a)
    (for {
        mr <- cr.modules
        m = mr.module
        (a, _) <- mr.artifacts
        if (!inFeatures.contains((m,a)))
      } yield toBundle(m,a)).toSet
  }

  def satisfies(constraint: FeatureRef, feature: Feature): Boolean =
    constraint.name == feature.name && (
      constraint.version.isEmpty || {
        var vr = constraint.version.get
        !vr.isEmpty() && (feature.version == Version.emptyVersion || vr.includes(feature.version))
      })

  def selectFeatureDeps(ref: FeatureRef, fs: Set[Feature]): Set[FeatureRef] =
    fs.filter(satisfies(ref, _)).flatMap(_.deps).collect { case dep: FeatureRef => dep }

  def selectFeatures(requested: Set[FeatureRef], fs: Set[Feature]): Either[Set[FeatureRef], Set[Feature]] = {
    val unsatisfied = for {
      constraint <- requested
      if (fs.forall(f => !satisfies(constraint, f)))
    } yield constraint
    if (unsatisfied.nonEmpty) Left(unsatisfied)
    else Right(
      for {
        constraint <- requested
        feature <- fs
        if (satisfies(constraint, feature))
      } yield feature
    )
  }

  @tailrec
  def resolveFeatures(requested: Set[FeatureRef], fs: Set[Feature], resolved: Set[Feature] = Set.empty): Either[Set[FeatureRef], Set[Feature]] = {
    if (requested.isEmpty) return Right(resolved)
    val result = selectFeatures(requested, fs)
    if (result.isLeft) result
    else {
      val Right(selection) = result
      val selectedRefs = selection.map(_.toRef)
      val resolvedRefs = resolved.map(_.toRef)
      val resolved2 = selection ++ resolved
      val unresolved = selectedRefs.flatMap(selectFeatureDeps(_, fs)) -- resolvedRefs
      resolveFeatures(unresolved, fs, resolved2)
    }
  }

}