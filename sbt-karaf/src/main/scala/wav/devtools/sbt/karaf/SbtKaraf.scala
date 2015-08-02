package wav.devtools.sbt.karaf

import sbt._
import sbt.Keys._
import wav.devtools.karaf.mbeans._, MBeanExtensions._

trait Import extends packaging.Import {

  object KarafKeys {

    lazy val karafContainerArgs = settingKey[ContainerArgs.Password]("The remote Karaf container where an instance will be created.")
    lazy val karafContainerServices = taskKey[MBeanServices]("The MBean services for the Karaf container")
    lazy val karafInstanceArgs = settingKey[Option[CreateInstanceArgs]]("The Karaf instance to which the project's features.xml will be deployed.")
    lazy val karafInstanceServices = taskKey[MBeanServices]("The MBean services for the Karaf instance")
    lazy val karafDestroyInstance = taskKey[Unit]("Destroy the Karaf instance")
    lazy val karafRefreshBundle = taskKey[Unit]("Refresh the project's bundle")
    lazy val karafBundleStartArgs = settingKey[BundleStartArgs]("Refresh the project's bundle")
    lazy val karafDeployFeature = taskKey[Unit]("Deploy the project's features.xml to the configured karaf container")
    lazy val karafUndeployFeature = taskKey[Unit]("Undeploy the project's features.xml in the configured karaf container")

  }
  
  object KarafSettings {

    import KarafPackagingSettings._

    lazy val bundleStartArgs = KarafKeys.karafBundleStartArgs := BundleStartArgs(organization.value + "." + name.value)

    lazy val containerArgs = KarafKeys.karafContainerArgs := ContainerArgs.Default

    // TODO
    lazy val instanceArgs = KarafKeys.karafInstanceArgs := Some(CreateInstanceArgs(s"sbt-${name.value}", (target.value / "karaf-instance").getCanonicalPath))

    lazy val containerServices = KarafKeys.karafContainerServices := {
      val containerArgs = KarafKeys.karafContainerArgs.value
      MBeanConnection(KarafKeys.karafContainerArgs.value).services
    }

    lazy val instanceServices = KarafKeys.karafInstanceServices := {
      val services = KarafKeys.karafContainerServices.value
      val instanceArgs = KarafKeys.karafInstanceArgs.value
      if (instanceArgs.isDefined) {
        val containerArgs = KarafKeys.karafContainerArgs.value
        val KarafServiceUrl(containerUrl) = containerArgs.serviceUrl
        val args = instanceArgs.get.copy(rmiRegistryPort = containerUrl.port)
        val instances = handled(services.Instances)
        val instance = {
          val result = instances.getInstance(args.name) orElse instances.create(args)
          if (result.isEmpty) sys.error(s"Couldn't find or create instance named `${args.name}` in container ${containerArgs.serviceUrl}")
          result.get
        }
        val instanceUrl = KarafServiceUrl(containerUrl.host, instance.rmiRegistryPort, instance.name).toString
        if (instance.state == "Stopped") instances.startInstance(args.name)
        MBeanConnection(ContainerArgs.Password(instanceUrl, containerArgs.user, containerArgs.pass)).services
      } else {
        services
      }
    }

    lazy val destroyInstance = KarafKeys.karafDestroyInstance := {
      val instances = handled(KarafKeys.karafContainerServices.value.Instances)
      val args = KarafKeys.karafInstanceArgs.value
      if (args.isDefined) {
        val name = args.get.name
        if (instances.getInstance(name).isDefined) {
          instances.stopInstance(name)
          instances.destroyInstance(name)
        }
      }
    }

    lazy val refreshBundle = KarafKeys.karafRefreshBundle := {
      val bundles = handled(KarafKeys.karafInstanceServices.value.Bundles)
      val startArgs = KarafKeys.karafBundleStartArgs.value
      val n = startArgs.name
      val original = {
        val result = bundles.bundles.find(_.name == n)
        if (result.isEmpty) sys.error("Couldn't find the project bundle, " + n)
        result.get
      }
      bundles.refresh(original.bundleId.toString)
      if (startArgs.startState == BundleState.Active) {
        bundles.start(original.bundleId.toString)
      }
      val updated = {
        val result = bundles.bundles.find(_.name == n)
        if (result.isEmpty) sys.error("Couldn't find the project bundle, " + n)
        result.get
      }
      if (!BundleState.inState(startArgs.startState, updated.state))
        sys.error(s"Couldn't resolve the project bundle $n, state: ${updated.state}, expected: ${startArgs.startState}")
    }

    lazy val deployFeature = KarafKeys.karafDeployFeature := {
      val repo = "file:" + KarafPackagingKeys.featuresXml.value.getCanonicalPath
      val features = handled(KarafKeys.karafInstanceServices.value.FeaturesService)
      if (!features.repoRefresh(repo)) sys.error("Couldn't add repository, " + repo)
      if (!features.install(name.value, version.value, false)) sys.error("Couldn't install project feature")
    }

    lazy val undeployFeature = KarafKeys.karafUndeployFeature := {
      val repo = "file:" + KarafPackagingKeys.featuresXml.value.getCanonicalPath
      val features = handled(KarafKeys.karafInstanceServices.value.FeaturesService)
      if (!features.uninstall(name.value, version.value)) sys.error("Couldn't uninstall project feature")
      if (!features.repoRemove(repo)) sys.error("Couldn't remove repository, " + repo)
    }

  }

}

trait SbtKarafSettings {

  val autoImport: Import

  import autoImport._

  import KarafPackagingSettings._

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
    generateDependsFileTask,
    resourceGenerators in Compile <+= Def.task {
      val f = (resourceManaged in Compile).value / packaging.model.DependenciesProperties.jarPath
      IO.copyFile(KarafPackagingKeys.generateDependsFile.value,f)
      Seq(f)
    })

  import KarafSettings._

  lazy val karafSettings: Seq[Setting[_]] = Seq(
    bundleStartArgs,
    containerArgs,
    KarafKeys.karafInstanceArgs := None,
    containerServices,
    instanceServices,
    destroyInstance,
    deployFeature,
    undeployFeature,
    refreshBundle,
    KarafKeys.karafRefreshBundle <<= KarafKeys.karafRefreshBundle dependsOn(KarafKeys.karafDeployFeature))

}

/**
 * There are 3 sets of functionality provided by this plugin.
 * - `SbtKaraf.featuresSettings` is for creating a features.xml artifact (default)
 * - `SbtKaraf.paxSettings` is for running a pax test with the pax version defined in this plugin
 * - `SbtKaraf.karafSettings` is for refreshing bundles and features on compile by
 *   managing a karaf instance (via. RMI on a local jvm)
 */
object SbtKaraf extends AutoPlugin with packaging.SbtKarafPackagingSettings with SbtKarafSettings {

  val autoImport = new Import {}

  override def projectSettings: Seq[Setting[_]] =
    featuresSettings ++ karafSettings

}