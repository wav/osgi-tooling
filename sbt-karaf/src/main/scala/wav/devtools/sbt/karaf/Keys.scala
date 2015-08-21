package wav.devtools.sbt.karaf

import sbt._
import wav.devtools.karaf.mbeans._
import wav.devtools.sbt.karaf.packaging._

object KarafKeys {

    lazy val karafContainerArgs = settingKey[ContainerArgs.Password]("The remote Karaf container where an instance will be created.")
    lazy val karafContainerServices = taskKey[MBeanServices]("The MBean services for the Karaf container")
    lazy val karafRefreshBundle = taskKey[Unit]("Refresh the project's bundle")
    lazy val karafBundleStartArgs = settingKey[BundleStartArgs]("Refresh the project's bundle")
    lazy val karafDeployFeature = taskKey[Unit]("Deploy the project's features.xml to the configured karaf container")
    lazy val karafUndeployFeature = taskKey[Unit]("Undeploy the project's features.xml in the configured karaf container")

}

trait Import extends packaging.Import {

  import KarafDefaults._

  val KarafKeys = wav.devtools.sbt.karaf.KarafKeys

  def defaultKarafSettings: Seq[Setting[_]] =
    defaultKarafPackagingSettings ++
      karafSettings

}

/**
 * There are 3 sets of functionality provided by this plugin.
 * - `SbtKaraf.featuresSettings` is for creating a features.xml artifact (default)
 * - `SbtKaraf.paxSettings` is for running a pax test with the pax version defined in this plugin
 * - `SbtKaraf.karafSettings` is for refreshing bundles and features on compile by
 *   managing a karaf instance (via. RMI on a local jvm)
 */
object SbtKaraf extends AutoPlugin {

  val autoImport = new Import {}

  override lazy val projectSettings =
    autoImport.defaultKarafSettings

}