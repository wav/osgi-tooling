package wav.devtools.sbt.karaf.packaging

import sbt._
import sbt.Keys._
import wav.devtools.sbt.karaf.packaging.model._

object KarafPackagingKeys {

  import FeaturesXml._

  lazy val featuresXml             = taskKey[FeaturesXml]("The project's features repository")
  lazy val featuresFile            = taskKey[Option[File]]("Generate features.xml")
  lazy val featuresRequired        = settingKey[Map[String, String]]("Features that will be put in the project feature")
  lazy val featuresRepositories    = taskKey[Set[Repository]]("Repositories where `featuresRequired` are specified")
  lazy val featuresSelected        = taskKey[Either[Set[Dependency], Set[Feature]]]("Resolved features or unsatisfied feature constraints")
  lazy val featuresProjectBundle   = taskKey[Bundle]("The project bundle to add to the project feature")
  lazy val featuresProjectFeature  = taskKey[Feature]("The project feature to add to features.xml")
  lazy val featuresAddDependencies = settingKey[Boolean](
    s"""Add bundles in feature repositories.
       |Warning: Attempt to download feature repositories before the project is loading.""".stripMargin)

  /**
   * When running pax exam in maven, the maven-depends-plugin generates a dependencies.properties file
   * that allows the user to use the `.versionAsInProject()` method that reads from that file.
   * This task generates this file so tests can use that functionality.
   */
  lazy val generateDependsFile = taskKey[File](s"${DependenciesProperties.jarPath} from `depends-maven-plugin`")

}

object SbtKarafPackaging extends AutoPlugin {

  object autoImport extends PluginSyntax {

    val KarafPackagingKeys = wav.devtools.sbt.karaf.packaging.KarafPackagingKeys

    def defaultKarafPackagingSettings: Seq[Setting[_]] =
      KarafPackagingDefaults.featuresSettings

    def addDependenciesInFeaturesRepositoriesSettings: Seq[Setting[_]] =
      Seq(onLoad in Global ~= (Internal.addDependenciesInFeaturesRepositories compose _))

  }

  override def globalSettings =
    autoImport.addDependenciesInFeaturesRepositoriesSettings

  override def projectSettings =
    autoImport.defaultKarafPackagingSettings

}