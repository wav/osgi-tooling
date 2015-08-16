package wav.devtools.sbt.karaf.packaging

import sbt._
import wav.devtools.sbt.karaf.packaging.model._

trait Import {

  import FeaturesXml._

  lazy val featuresXml            = taskKey[FeaturesXml]("The project's features repository")
  lazy val generateFeaturesFile   = taskKey[Option[File]]("Generate features.xml")
  lazy val featuresRequired       = settingKey[Map[String, String]]("Features that will be put in the project feature")
  lazy val featuresRepositories   = taskKey[Set[FeaturesRepository]]("All resolved features repositories")
  lazy val featuresSelected       = taskKey[Set[Feature]]("All resolved feature definitions")
  lazy val featuresProjectBundle  = taskKey[Bundle]("The project bundle to add to the project feature")
  lazy val featuresProjectFeature = taskKey[Feature]("The project feature to add to features.xml")

  /**
   * When running pax exam in maven, the maven-depends-plugin generates a dependencies.properties file
   * that allows the user to use the `.versionAsInProject()` method that reads from that file.
   * This task generates this file so tests can use that functionality.
   */
  lazy val generateDependsFile = taskKey[File](s"${DependenciesProperties.jarPath} from `depends-maven-plugin`")

}

object KarafPackagingKeys extends Import

object SbtKarafPackaging extends AutoPlugin {

  val autoImport = new Import {}

  override def projectSettings: Seq[Setting[_]] =
    KarafPackagingDefaults.featuresSettings

}