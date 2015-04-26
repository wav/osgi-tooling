package wav.devtools.sbt.karaf

import sbt.Keys._
import sbt._
import wav.devtools.karafmanagement.MBeanExtensions._
import wav.devtools.karafmanagement._
import wav.devtools.sbt.karaf.model.{DependenciesProperties => DProps, BundleStartArgs, FeaturesXml}

object Import {

  object KarafKeys {

    lazy val featuresXml = taskKey[File]("Generate features.xml")
    lazy val featuresProjectBundle = settingKey[(BundleStartArgs, FeaturesXml.Bundle)]("The project bundle to add to the project feature")
    lazy val featuresProjectFeature = settingKey[Option[FeaturesXml.Feature]]("The project feature to add to features.xml")
    lazy val featuresScalaFeature = settingKey[FeaturesXml.Feature]("The scala feature to add to features.xml")
    lazy val featuresElements = settingKey[Seq[FeaturesXml.Feature]]("Elements to add to features.xml")
    lazy val featuresProperties = taskKey[Map[String, String]]("Generate properties to inject into features.xml")

    lazy val karafContainerArgs = settingKey[ContainerArgs.Password]("The remote Karaf container where an instance will be created.")
    lazy val karafContainerServices = taskKey[MBeanServices]("The MBean services for the Karaf container")
    lazy val karafInstanceArgs = settingKey[CreateInstanceArgs]("The Karaf instance to which the project's features.xml will be deployed.")
    lazy val karafInstanceServices = taskKey[MBeanServices]("The MBean services for the Karaf instance")
    lazy val karafDestroyInstance = taskKey[Unit]("Destroy the Karaf instance")
    lazy val karafRefreshBundle = taskKey[Unit]("Refresh the project's bundle")
    lazy val karafDeployFeature = taskKey[Unit]("Deploy the project's features.xml to the configured karaf container")
    lazy val karafUndeployFeature = taskKey[Unit]("Undeploy the project's features.xml in the configured karaf container")

    /**
     * When running pax exam in maven, the maven-depends-plugin generates a dependencies.properties file
     * that allows the user to use the `.versionAsInProject()` method that reads from that file.
     * This task generates this file so tests can use that functionality.
     */
    lazy val paxGenerateDependsFile = taskKey[File](s"${DProps.jarPath} from `depends-maven-plugin`")
  }

  lazy val defaultContainerArgs = KarafKeys.karafContainerArgs := ContainerArgs.Default

  lazy val defaultInstanceArgs = KarafKeys.karafInstanceArgs := CreateInstanceArgs(s"sbt-${name.value}", (target.value / "karaf-instance").getCanonicalPath)

  lazy val containerServices = KarafKeys.karafContainerServices := {
    val containerArgs = (KarafKeys.karafContainerArgs).value
    MBeanConnection((KarafKeys.karafContainerArgs).value).services
  }

  lazy val instanceServices = KarafKeys.karafInstanceServices := {
    val instances = handled((KarafKeys.karafContainerServices).value.Instances)
    val instanceArgs = (KarafKeys.karafInstanceArgs).value
    val containerArgs = (KarafKeys.karafContainerArgs).value
    val instance = {
      val result = instances.getInstance(instanceArgs.name) orElse instances.create(instanceArgs)
      if (result.isEmpty) sys.error(s"Couldn't find or create instance named: ${instanceArgs.name} container ${containerArgs.serviceUrl}")
      result.get
    }
    instances.startInstance(instanceArgs.name)
    val KarafServiceUrl(containerUrl) = containerArgs.serviceUrl
    MBeanConnection(ContainerArgs.Password(
      KarafServiceUrl(containerUrl.host, instance.rmiRegistryPort, instance.name).toString, containerArgs.user, containerArgs.pass
    )).services
  }

  lazy val destroyInstance = KarafKeys.karafDestroyInstance := {
    val instances = handled((KarafKeys.karafContainerServices).value.Instances)
    val instanceArgs = (KarafKeys.karafInstanceArgs).value
    instances.destroyInstance(instanceArgs.name)
  }

  lazy val refreshBundle = KarafKeys.karafRefreshBundle := {
    val bundles = handled((KarafKeys.karafInstanceServices).value.Bundles)
    val (startArgs, _) = (KarafKeys.featuresProjectBundle).value
    val n = startArgs.name
    val original = {
      val result = bundles.bundles.find(_.name == n)
      if (result.isEmpty) sys.error("Couldn't find the project bundle, " + n)
      result.get
    }
    bundles.refresh(original.bundleId.toString)
    if (startArgs.startState == startArgs.Started.startState) {
      bundles.start(original.bundleId.toString)
    }
    val updated = {
      val result = bundles.bundles.find(_.name == n)
      if (result.isEmpty) sys.error("Couldn't find the project bundle, " + n)
      result.get
    }
    if (updated.state != startArgs.startState) sys.error(s"Couldn't resolve the project bundle $n, state: ${updated.state}")
  }

  lazy val generateDependsFile = KarafKeys.paxGenerateDependsFile := {
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

  lazy val deployFeature = KarafKeys.karafDeployFeature := {
    val repo = "file:" + (KarafKeys.featuresXml).value.getCanonicalPath
    val features = handled((KarafKeys.karafInstanceServices).value.FeaturesService)
    if (!features.repoAdd(repo)) sys.error("Couldn't add repository, " + repo)
    if (!features.install(name.value, version.value, false)) sys.error("Couldn't install project feature")
  }

  lazy val undeployFeature = KarafKeys.karafUndeployFeature := {
    val repo = "file:" + (KarafKeys.featuresXml).value.getCanonicalPath
    val features = handled((KarafKeys.karafInstanceServices).value.FeaturesService)
    if (!features.uninstall(name.value)) sys.error("Couldn't uninstall project feature")
    if (!features.repoRemove(repo)) sys.error("Couldn't remove repository, " + repo)
  }

  lazy val generateFeatures = KarafKeys.featuresXml := {
    val featuresTarget = target.value / "features.xml"
    val featuresSource = (resourceDirectory in Compile).value / "features.xml"
    util.Xml.write(
      featuresTarget,
      featuresSource,
      FeaturesXml.XSD,
      KarafKeys.featuresProperties.value,
      FeaturesXml.toXml(
        name.value,
        (KarafKeys.featuresElements).value))
  }

  lazy val bundle = KarafKeys.featuresProjectBundle := {
    val pName = name.value
    val oName = organization.value
    val pVersion = version.value
    (BundleStartArgs(pName), FeaturesXml.Bundle(s"mvn:$oName:$pName:$pVersion"))
  }

  lazy val feature = KarafKeys.featuresProjectFeature := {
    import FeaturesXml._
    val scfeat = (KarafKeys.featuresScalaFeature).value
    val (_, pbundle) = (KarafKeys.featuresProjectBundle).value
    Some(
      Feature(name.value, Some(version.value),
        Set(scfeat.Ref, pbundle)))
  }

  lazy val features = KarafKeys.featuresElements := {
      val pFeat = (KarafKeys.featuresProjectFeature).value
      val scFeat = (KarafKeys.featuresScalaFeature).value
      if (pFeat.isDefined) Seq(pFeat.get, scFeat) else Seq()
  }

  lazy val scalaFeature = KarafKeys.featuresScalaFeature := {
    import FeaturesXml._
    val scVersion = (scalaVersion in ThisBuild).value
    val scbVersion = (scalaBinaryVersion in ThisBuild).value
    Feature("scala-library", Some(scbVersion), Set(
      Bundle(s"mvn:org.scala-lang:scala-library:$scVersion")))
  }

}

/**
 * There are 3 sets of functionality provided by this plugin.
 * - `SbtKaraf.featuresSettings` is for creating a features.xml artifact (default)
 * - `SbtKaraf.paxSettings` is for running a pax test with the pax version defined in this plugin
 * - `SbtKaraf.karafSettings` is for refreshing bundles and features on compile by
 *   managing a karaf instance (via. RMI on a local jvm)
 */
object SbtKaraf extends AutoPlugin {

  val autoImport = Import

  import autoImport._

  override def projectSettings: Seq[Setting[_]] =
    featuresSettings

  /**
   * Inorder to get pax test to run, you only need:
   * - Pax dependencies for a container as per the pax exam website
   * - A JUnit test framework must be registered with SBT
   */
  lazy val paxSettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      import wav.devtools.sbt.karaf.Dependencies._
      Seq(paxExam, paxKaraf, paxAether, javaxInject, paxJunit, junit, junitInterface, osgiCore, Karaf.`package`)
    },
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"), // for junit-interface / paxExam
    fork in Test := true, // IMPORTANT, forking ensures that the container starts with the correct classpath
    outputStrategy in Test := Some(StdoutOutput),
    generateDependsFile,
    resourceGenerators in Compile <+= Def.task {
      val f = (resourceManaged in Compile).value / DProps.jarPath
      IO.copyFile(KarafKeys.paxGenerateDependsFile.value,f)
      Seq(f)
    })

  lazy val featuresSettings: Seq[Setting[_]] = Seq(
    bundle,
    feature,
    scalaFeature,
    features,
    KarafKeys.featuresProperties := Map(),
    generateFeatures,
    packagedArtifacts <<= Def.task {
      packagedArtifacts.value.updated(
        Artifact(name.value, `type` = "xml", extension = "xml", classifier = "features"),
        (KarafKeys.featuresXml).value)
    })

  lazy val karafSettings: Seq[Setting[_]] = Seq(
    defaultContainerArgs,
    defaultInstanceArgs,
    containerServices,
    instanceServices,
    destroyInstance,
    deployFeature,
    undeployFeature,
    refreshBundle,
    KarafKeys.karafRefreshBundle <<= (KarafKeys.karafRefreshBundle) dependsOn(KarafKeys.karafDeployFeature))

}