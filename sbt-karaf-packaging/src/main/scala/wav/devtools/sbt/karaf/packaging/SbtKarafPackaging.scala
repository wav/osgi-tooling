package wav.devtools.sbt.karaf.packaging

import model.{DependenciesProperties => DProps, FeaturesXml}

import sbt.Keys._
import sbt._

trait Import {

  import FeaturesXml._

  object KarafPackagingKeys {

    lazy val featuresXml = taskKey[File]("Generate features.xml")
    lazy val featuresProjectBundle = taskKey[FeaturesXml.Bundle]("The project bundle to add to the project feature")
    lazy val featuresProjectFeature = taskKey[FeaturesXml.Feature]("The project feature to add to features.xml")
    lazy val featuresScalaFeature = settingKey[FeaturesXml.Feature]("The scala feature to add to features.xml")
    lazy val featuresElements = taskKey[Seq[FeaturesXml.Feature]]("Elements to add to features.xml")
    lazy val featuresProperties = taskKey[Map[String, String]]("Generate properties to inject into features.xml")

    /**
     * When running pax exam in maven, the maven-depends-plugin generates a dependencies.properties file
     * that allows the user to use the `.versionAsInProject()` method that reads from that file.
     * This task generates this file so tests can use that functionality.
     */
    lazy val generateDependsFile = taskKey[File](s"${DProps.jarPath} from `depends-maven-plugin`")

  }

  object KarafPackagingSettings {

    import KarafPackagingKeys._

    lazy val featuresXmlTask = featuresXml := {
      val featuresTarget = crossTarget.value / "features.xml"
      val featuresSource = (resourceDirectory in Compile).value / "features.xml"
      Util.write(
        featuresTarget,
        featuresSource,
        XSD,
        featuresProperties.value,
        toXml(
          name.value,
          featuresElements.value))
    }

    lazy val featuresProjectBundleTask = featuresProjectBundle := {
      val (_, f) = (packagedArtifact in(Compile, packageBin)).value
      Bundle(f.toURI.toString)
    }

    lazy val featuresProjectFeatureTask = featuresProjectFeature :=
      Feature(name.value, Some(version.value),
        Set(featuresScalaFeature.value.toRef,
          featuresProjectBundle.value))

    lazy val featuresElementsTask = featuresElements := Seq(
      featuresProjectFeature.value,
      featuresScalaFeature.value)

    lazy val featuresScalaFeatureTask = featuresScalaFeature := {
      val scVersion = scalaVersion.value
      val scbVersion = scalaBinaryVersion.value
      Feature("scala-library", Some(scbVersion), Set(
        Bundle(s"mvn:org.scala-lang/scala-library/$scVersion")))
    }

    lazy val generateDependsFileTask = generateDependsFile := {
      val f = target.value / "dependencies.properties"
      val artifacts = for {
        conf <- update.value.configurations
        moduleReport <- conf.modules
        (a, _) <- moduleReport.artifacts
      } yield {
          val m = moduleReport.module
          DProps.Artifact(m.organization, a.name, m.revision, conf.configuration, a.`type`)
        }
      val fcontent = DProps(
        DProps.Project(organization.value, name.value, version.value),
        artifacts)
      IO.write(f, fcontent)
      f
    }

  }

}

trait SbtKarafPackagingSettings {

  val autoImport: Import

  import autoImport._

  import KarafPackagingSettings._

  lazy val featuresSettings: Seq[Setting[_]] = Seq(
    featuresXmlTask,
    featuresElementsTask,
    featuresProjectBundleTask,
    featuresScalaFeatureTask,
    featuresProjectFeatureTask,
    KarafPackagingKeys.featuresProperties := Map(),
    generateDependsFileTask,
    packagedArtifacts <<= Def.task {
      packagedArtifacts.value.updated(
        Artifact(name.value, `type` = "xml", extension = "xml", classifier = "features"),
        KarafPackagingKeys.featuresXml.value)
    })

}

object SbtKarafPackaging extends AutoPlugin with SbtKarafPackagingSettings {

  val autoImport = new Import {}

  override def projectSettings: Seq[Setting[_]] =
    featuresSettings

}