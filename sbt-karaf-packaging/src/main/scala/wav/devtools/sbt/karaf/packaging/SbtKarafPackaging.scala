package wav.devtools.sbt.karaf.packaging

import model.{DependenciesProperties => DProps, FeaturesXml}

import sbt.Keys._
import sbt._

trait Import {

  object KarafPackagingKeys {

    lazy val featuresXml = taskKey[File]("Generate features.xml")
    lazy val featuresProjectBundle = taskKey[FeaturesXml.Bundle]("The project bundle to add to the project feature")
    lazy val featuresProjectFeature = taskKey[Option[FeaturesXml.Feature]]("The project feature to add to features.xml")
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

    lazy val generateFeatures = KarafPackagingKeys.featuresXml := {
      val featuresTarget = crossTarget.value / "features.xml"
      val featuresSource = (resourceDirectory in Compile).value / "features.xml"
      Util.write(
        featuresTarget,
        featuresSource,
        FeaturesXml.XSD,
        KarafPackagingKeys.featuresProperties.value,
        FeaturesXml.toXml(
          name.value,
          KarafPackagingKeys.featuresElements.value))
    }

    lazy val bundle = KarafPackagingKeys.featuresProjectBundle := {
      val (_, f) = (packagedArtifact in(Compile, packageBin)).value
      FeaturesXml.Bundle(f.toURI.toString)
    }

    lazy val feature = KarafPackagingKeys.featuresProjectFeature :=
      Some(
        FeaturesXml.Feature(name.value, Some(version.value),
          Set(KarafPackagingKeys.featuresScalaFeature.value.Ref,
            KarafPackagingKeys.featuresProjectBundle.value)))

    lazy val features = KarafPackagingKeys.featuresElements := {
      val pFeat = KarafPackagingKeys.featuresProjectFeature.value
      val scFeat = KarafPackagingKeys.featuresScalaFeature.value
      if (pFeat.isDefined) Seq(pFeat.get, scFeat) else Seq()
    }

    lazy val scalaFeature = KarafPackagingKeys.featuresScalaFeature := {
      import FeaturesXml._
      val scVersion = scalaVersion.value
      val scbVersion = scalaBinaryVersion.value
      Feature("scala-library", Some(scbVersion), Set(
        Bundle(s"mvn:org.scala-lang/scala-library/$scVersion")))
    }

    lazy val generateDependsFile = KarafPackagingKeys.generateDependsFile := {
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
    bundle,
    feature,
    scalaFeature,
    features,
    KarafPackagingKeys.featuresProperties := Map(),
    generateFeatures,
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