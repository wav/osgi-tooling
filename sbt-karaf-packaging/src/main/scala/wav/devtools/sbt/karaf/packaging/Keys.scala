package wav.devtools.sbt.karaf.packaging

import sbt._
import wav.devtools.sbt.karaf.packaging.model._

object KarafPackagingKeys {

  import FeaturesXml._

  lazy val featuresXml            = taskKey[FeaturesXml]("The project's features repository")
  lazy val featuresFile           = taskKey[Option[File]]("Generate features.xml")
  lazy val featuresRequired       = settingKey[Map[String, String]]("Features that will be put in the project feature")
  lazy val featuresRepositories   = taskKey[Set[Repository]]("Repositories where `featuresRequired` are specified")
  lazy val featuresSelected       = taskKey[Either[Set[FeatureRef], Set[Feature]]]("Resolved features or unsatisfied feature constraints")
  lazy val featuresProjectBundle  = taskKey[Bundle]("The project bundle to add to the project feature")
  lazy val featuresProjectFeature = taskKey[Feature]("The project feature to add to features.xml")

  /**
   * When running pax exam in maven, the maven-depends-plugin generates a dependencies.properties file
   * that allows the user to use the `.versionAsInProject()` method that reads from that file.
   * This task generates this file so tests can use that functionality.
   */
  lazy val generateDependsFile = taskKey[File](s"${DependenciesProperties.jarPath} from `depends-maven-plugin`")

}

trait Import {

  import KarafPackagingDefaults._

  val KarafPackagingKeys = wav.devtools.sbt.karaf.packaging.KarafPackagingKeys
  val FeatureID          = wav.devtools.sbt.karaf.packaging.FeatureID _

  def defaultKarafPackagingSettings: Seq[Setting[_]] =
    featuresSettings
}

object SbtKarafPackaging extends AutoPlugin {

  val autoImport = new Import {}

  override def projectSettings =
    autoImport.defaultKarafPackagingSettings

}