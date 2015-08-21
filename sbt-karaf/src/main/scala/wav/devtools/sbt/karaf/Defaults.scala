package wav.devtools.sbt.karaf

import sbt.Keys._
import sbt._
import wav.devtools.karaf.mbeans._, MBeanExtensions._

object KarafDefaults {

  import KarafKeys._
  import packaging.KarafPackagingKeys._

  lazy val karafBundleArgsSetting = Def.setting(BundleStartArgs(organization.value + "." + name.value))

  lazy val karafContainerArgsSetting = Def.setting(ContainerArgs.Default)

  lazy val karafContainerServicesTask = Def.task {
    val containerArgs = karafContainerArgs.value
    MBeanConnection(karafContainerArgs.value).services
  }

  lazy val karafRefreshBundleTask = Def.task {
    val bundles = handled(karafContainerServices.value.Bundles)
    val startArgs = karafBundleStartArgs.value
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

  lazy val karafDeployFeatureTask = Def.task {
    val ff = featuresFile.value
    require(ff.isDefined, "`featuresFile` must produce a features file")
    val repo = "file:" + ff.get.getCanonicalPath
    val features = handled(karafContainerServices.value.FeaturesService)
    if (!features.repoRefresh(repo)) sys.error("Couldn't add repository, " + repo)
    if (!features.install(name.value, version.value, false)) sys.error("Couldn't install project feature")
  }

  lazy val karafUndeployFeatureTask = Def.task {
    val ff = featuresFile.value
    require(ff.isDefined, "`featuresFile` must produce a features file")
    val repo = "file:" + ff.get.getCanonicalPath
    val features = handled(karafContainerServices.value.FeaturesService)
    if (!features.uninstall(name.value, version.value)) sys.error("Couldn't uninstall project feature")
    if (!features.repoRemove(repo)) sys.error("Couldn't remove repository, " + repo)
  }

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
    resourceGenerators in Compile <+= Def.task {
      val f = (resourceManaged in Compile).value / packaging.model.DependenciesProperties.jarPath
      IO.copyFile(generateDependsFile.value,f)
      Seq(f)
    })

  lazy val karafSettings: Seq[Setting[_]] = Seq(
    karafBundleStartArgs := karafBundleArgsSetting.value,
    karafContainerArgs := karafContainerArgsSetting.value,
    karafContainerServices := karafContainerServicesTask.value,
    karafDeployFeature := karafDeployFeatureTask.value,
    karafUndeployFeature := karafUndeployFeatureTask.value,
    karafRefreshBundle := karafRefreshBundleTask.value,
    karafRefreshBundle <<= karafRefreshBundle dependsOn(karafDeployFeature))

}