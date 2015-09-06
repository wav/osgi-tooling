package wav.devtools.sbt.karaf.packaging.model

import java.io.File

import wav.devtools.sbt.karaf.packaging.Util
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._

import scala.util.{Failure, Success, Try}
import scala.xml.XML

// An artifact in a features file.
sealed trait FeaturesArtifactData {

  val module  : sbt.ModuleID
  val artifact: sbt.Artifact
  val file    : Option[File]
  val from    : Option[(String, Option[String])]

  lazy val downloaded: Boolean =
    file.isDefined && file.get.exists()

  lazy val url: String =
    artifact.url.map(_.toString) getOrElse mavenUrl.toString

  lazy val mavenUrl: MavenUrl =
    MavenUrl(module.organization, artifact.name, module.revision, Some(artifact.extension), artifact.classifier)

  private [packaging] def equalsData(that: FeaturesArtifactData): Boolean =
    this.module == that.module && this.artifact == that.artifact

  def copyData(module: sbt.ModuleID = module, artifact: sbt.Artifact = artifact, file: Option[File] = file): FeaturesArtifact =
    new FeaturesArtifact(module, artifact, file, from)

}

object FeaturesArtifactData {

  private [packaging] def diff(as: Seq[FeaturesArtifactData], bs: Seq[FeaturesArtifactData]): Set[FeaturesArtifactData] =
    as.filterNot(a => bs.exists(b => a equalsData b)).toSet

  def canBeFeaturesRepository(artifact: sbt.Artifact): Boolean =
    artifact.extension == "xml" && artifact.classifier == Some("features")

  def canBeOSGiBundle(artifact: sbt.Artifact): Boolean =
    artifact.extension == "jar" && (artifact.`type` == "jar" || artifact.`type` == "bundle")

  private [packaging] def isValidOSGiBundle(file: File): Boolean =
    Util.getJarManifest(file.toString)
      .getMainAttributes
      .containsKey("Bundle-SymbolicName")

  private def isValidFeaturesXml(file: File): Boolean =
    if (!file.exists()) false
    else Try(Util.validateXml(file.getCanonicalPath, getClass.getResourceAsStream("/" + FeaturesXmlFormats.featuresXsd))) match {
      case Failure(ex) => println(ex); false
      case Success(_) => true
    }

  def readFeaturesXml(file: File): Option[FeaturesXml] =
    if (!isValidFeaturesXml(file)) None
    else FeaturesXmlFormats.readFeaturesXml(XML.loadFile(file))

}

case class FeaturesArtifact(module: sbt.ModuleID, artifact: sbt.Artifact, file: Option[File], from: Option[(String, Option[String])] = None)
  extends FeaturesArtifactData {

  import FeaturesArtifactData._

  def toBundle: Option[OSGiBundle] =
    if (downloaded && canBeOSGiBundle(artifact) && isValidOSGiBundle(file.get))
      Some(new OSGiBundle(module, artifact, file, from))
    else None

  def toRepository: Option[FeatureRepository] =
    if (!downloaded) None
    else readFeaturesXml(file.get).map(new FeatureRepository(module, artifact, file, _))

}

case class OSGiBundle(
  module: sbt.ModuleID,
  artifact: sbt.Artifact,
  file: Option[File],
  from: Option[(String, Option[String])])
  extends FeaturesArtifactData

case class FeatureRepository(
  module: sbt.ModuleID,
  artifact: sbt.Artifact,
  file: Option[File],
  featuresXml: FeaturesXml)
  extends FeaturesArtifactData {

  override def toString(): String = {
    s"FeaturesRepository(${featuresXml.name},$module/${artifact.name},$file,$features})"
  }

  val from = None

  lazy val repositories: Set[String] =
    featuresXml.elems.collect { case Repository(url) => url }.toSet

  lazy val features: Set[Feature] =
    featuresXml.elems.collect { case f: Feature => f }.toSet

}