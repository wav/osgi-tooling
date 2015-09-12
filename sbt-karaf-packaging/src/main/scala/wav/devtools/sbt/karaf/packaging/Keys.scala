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
       |Warning: Attempts to download feature descriptors before the project is loading.""".stripMargin)

  /**
   * Usage hint: makes the use of `.versionAsInProject()` available in pax-exam tests
   */
  lazy val shouldGenerateDependsFile = settingKey[Boolean]("Generate a dependencies.properties file like the `maven-depends-plugin`")

}

object SbtKarafPackaging extends AutoPlugin {

  object autoImport extends PluginSyntax {

    val KarafPackagingKeys = wav.devtools.sbt.karaf.packaging.KarafPackagingKeys

    def defaultKarafPackagingSettings: Seq[Setting[_]] =
      KarafPackagingDefaults.featuresSettings

    def addDependenciesInFeaturesRepositoriesSettings: Seq[Setting[_]] =
      Seq(onLoad in Global ~= (Internal.addDependenciesInFeaturesRepositories compose _))

  }

  override def requires =
    sbt.plugins.MavenResolverPlugin

  override def globalSettings =
    autoImport.addDependenciesInFeaturesRepositoriesSettings

  override def projectSettings =
    autoImport.defaultKarafPackagingSettings

}