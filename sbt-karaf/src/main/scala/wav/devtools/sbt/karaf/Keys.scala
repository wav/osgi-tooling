package wav.devtools.sbt.karaf

import javax.management.remote.JMXConnector

import sbt._
import wav.devtools.karaf.mbeans._

object KarafKeys {

  lazy val karafResetServer     = taskKey[Unit]("Restart the server and clean data and caches")
  lazy val karafStatus          = taskKey[Unit]("Relevant status information")
  lazy val karafContainerArgs   = settingKey[ContainerArgs.Password]("The remote Karaf container to connect to.")
  lazy val karafRefreshBundle   = taskKey[Unit]("Refresh the project's bundle")
  lazy val karafBundleStartArgs = settingKey[BundleStartArgs]("Refresh the project's bundle")
  lazy val karafDeployFeature   = taskKey[Unit]("Deploy the project's features.xml to the configured karaf container")
  lazy val karafUndeployFeature = taskKey[Unit]("Undeploy the project's features.xml in the configured karaf container")

}

/**
 * There are 3 sets of functionality provided by this plugin.
 * - `featuresSettings` is for creating a features.xml artifact (default)
 * - `paxSettings` is for running a pax test with the pax version defined in this plugin
 * - `karafSettings` is for refreshing bundles and features on compile by
 * managing a karaf instance (via. RMI on a local jvm)
 */
object SbtKaraf extends AutoPlugin {

  object autoImport {

    import packaging.SbtKarafPackaging.autoImport._

    val KarafKeys = wav.devtools.sbt.karaf.KarafKeys

    def defaultKarafSettings: Seq[Setting[_]] =
      defaultKarafPackagingSettings ++
        KarafDefaults.karafSettings
  }

  override def requires =
    packaging.SbtKarafPackaging

  override lazy val projectSettings =
    autoImport.defaultKarafSettings

}