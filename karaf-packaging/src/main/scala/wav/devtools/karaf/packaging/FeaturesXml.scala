package wav.devtools.karaf.packaging

import org.osgi.framework.{Version, VersionRange}

object FeaturesXml {

  sealed trait FeaturesOption

  case class Repository(url: String)
    extends FeaturesOption

  sealed trait FeatureOption

  implicit def string2someVersion(version: String): Option[Version] =
    Some(Version.parseVersion(version))

  case class Feature(
    name: String,
    version: Version = Version.emptyVersion,
    deps: Set[FeatureOption] = Set.empty,
    description: Option[String] = None
  ) extends FeaturesOption {
    def toDep: Dependency =
      Dependency(name, if (version == Version.emptyVersion) None else Some(new VersionRange(version.toString())))
  }

  sealed trait ConditionalOption

  case class Bundle(
    url: String,
    dependency: Boolean = true,
    start: Boolean = false,
    `start-level`: Option[Int] = None
    ) extends FeatureOption with ConditionalOption

  // Wrapping a bundle doesn't require instructions, if none are provided defaults are set by the runtime.
  // https://ops4j1.jira.com/wiki/display/paxurl/Wrap+Protocol#WrapProtocol-defaultinstructions
  def WrappedBundle(
    url: String,
    instructions: Map[String, String] = Map.empty,
    instructionsUrl: Option[String] = None,
    dependency: Boolean = true,
    start: Boolean = false,
    `start-level`: Option[Int] = None): Bundle = {
    val instUrl = if (instructionsUrl.nonEmpty) ("," + instructionsUrl.get) else ""
    val inst = if (instructions.nonEmpty) ("$" + instructions.map(e => e._1 + "=" + e._2).mkString("&")) else ""
    Bundle("wrap:" + url + instUrl + inst, dependency, start, `start-level`)
  }

  implicit def string2someVersionRange(version: String): Option[VersionRange] =
    Some(new VersionRange(version))

  case class Dependency(
    name: String,
    version: Option[VersionRange] = None,
    prerequisite: Boolean = true,
    dependency: Boolean = true
    ) extends FeatureOption

  case class Config(
    name: String,
    value: String,
    append: Boolean = true
    ) extends FeatureOption with ConditionalOption

  case class ConfigFile(
    finalname: String,
    value: String,
    overrideValue: Boolean = true
    ) extends FeatureOption with ConditionalOption

  case class Conditional(
    condition: String,
    deps: Set[ConditionalOption]
    ) extends FeatureOption

  def feature(name: String, version: String, deps: Set[FeatureOption] = Set.empty): Feature =
    Feature(name, Version.parseVersion(version), deps)

  val emptyFeature = Feature(null)

  val emptyFeatureRef = Dependency(null)

}

case class FeaturesXml(name: String, elems: Seq[FeaturesXml.FeaturesOption] = Nil) {
  import FeaturesXml._
  lazy val repositories: Seq[Repository] = elems.collect { case f: Repository => f }
  lazy val features: Seq[Feature] = elems.collect { case f: Feature => f }
}