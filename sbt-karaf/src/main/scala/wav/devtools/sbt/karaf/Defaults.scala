package wav.devtools.sbt.karaf

import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import javax.management.remote.JMXConnector

import sbt.Keys._
import sbt._
import wav.devtools.karaf.mbeans._, MBeanExtensions._
import KarafKeys._
import packaging.KarafPackagingKeys._

import scala.util.{Failure, Success}

object KarafDefaults {

//  lazy val lazy val karafDistribution      = taskKey[Option[File]]("Karaf distribution defined in library dependencies")      = taskKey[Option[File]]("Karaf distribution defined in library dependencies")
//
//  libraryDependencies += Dependencies.Karaf.assembly
//
//  lazy val karafDistributionTask = Def.task {
//    val km = Dependencies.Karaf.assembly
//    (for {
//      ka <- km.explicitArtifacts
//      if (ka.`type` == "tar.gz")
//      cr <- update.value.configurations
//      mr <- cr.modules
//      m = mr.module
//      if (m.organization == km.organization &&
//          m.name == km.name)
//      (a, f) <- mr.artifacts
//      if (ka.name == a.name &&
//          ka.`type` == a.`type`)
//    } yield f).headOption
//  }

  lazy val karafBundleArgsSetting = Def.setting(BundleStartArgs(organization.value + "." + name.value))

  lazy val karafContainerArgsSetting = Def.setting(DefaultContainerArgs)

  private val karafRMIConnection = settingKey[AtomicReference[Option[JMXConnector]]]("karaf RMI connection")

  def closeKarafConnection(ref: AtomicReference[Option[JMXConnector]]): Unit =
      ref.getAndSet(None).foreach(_.close())

  def getKarafServices(args: ContainerArgs, ref: AtomicReference[Option[JMXConnector]]): MBeanServices = {
    var c = ref.get()
    if (c.isDefined) c.get.services
    else {
      val c = MBeanConnection(args) match {
        case Failure(ex) =>
          ref.set(None)
          throw ex
        case Success(c) =>
          ref.set(Some(c))
          c
      }
      c.services
    }
  }

  lazy val karafResetServerTask = Def.task {
    val ref = karafRMIConnection.value
    val services = getKarafServices(karafContainerArgs.value, ref)
    val system = handled(services.System)
    system.rebootCleanAll("now")
    closeKarafConnection(ref)
  }

  lazy val karafRefreshBundleTask = Def.task {
    val services = getKarafServices(karafContainerArgs.value, karafRMIConnection.value)
    val bundles = handled(services.Bundles)
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
    val repo = ff.get.getAbsoluteFile.toURI.toString
    val services = getKarafServices(karafContainerArgs.value, karafRMIConnection.value)
    val features = handled(services.FeaturesService)
    if (!features.repoRefresh(repo)) sys.error("Couldn't add repository, " + repo)
    if (!features.install(name.value, version.value, false)) sys.error("Couldn't install project feature")
  }

  lazy val karafUndeployFeatureTask = Def.task {
    val ff = featuresFile.value
    require(ff.isDefined, "`featuresFile` must produce a features file")
    val repo = ff.get.getAbsoluteFile.toURI.toString
    val services = getKarafServices(karafContainerArgs.value, karafRMIConnection.value)
    val features = handled(services.FeaturesService)
    if (!features.uninstall(name.value, version.value)) sys.error("Couldn't uninstall project feature")
    if (!features.repoRemove(repo)) sys.error("Couldn't remove repository, " + repo)
  }

  /**
   * Inorder to get pax test to run, you only need:
   * - Pax dependencies for a container as per the pax exam website
   * - A JUnit test framework must be registered with SBT
   */
  lazy val paxSettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Dependencies.paxExamKaraf,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
    fork in Test := true, // IMPORTANT, forking ensures that the container starts with the correct classpath
    outputStrategy in Test := Some(StdoutOutput),
    resourceGenerators in Compile <+= Def.task {
      val f = (resourceManaged in Compile).value / packaging.model.DependenciesProperties.jarPath
      IO.copyFile(generateDependsFile.value,f)
      Seq(f)
    })

  lazy val arquillianSettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Dependencies.arquillianRemoteKaraf,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
  )

  lazy val karafSettings: Seq[Setting[_]] = Seq(
    karafRMIConnection := new AtomicReference(None),
    karafResetServer := karafResetServerTask.value,
    karafStatus := ???,
//    karafDistribution := karafDistributionTask.value,
    karafBundleStartArgs := karafBundleArgsSetting.value,
    karafContainerArgs := karafContainerArgsSetting.value,
    karafDeployFeature := karafDeployFeatureTask.value,
    karafUndeployFeature := karafUndeployFeatureTask.value,
    karafRefreshBundle := karafRefreshBundleTask.value,
    karafRefreshBundle <<= karafRefreshBundle dependsOn(karafDeployFeature))

}