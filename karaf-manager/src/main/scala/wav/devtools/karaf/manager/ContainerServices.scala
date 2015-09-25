package wav.devtools.karaf.manager

import java.net.URI
import javax.management.remote.JMXConnector
import wav.devtools.karaf.mbeans, mbeans._

import scala.util.Try

trait KarafJMXClient {

  val args: ContainerArgs

  private val connector: () => Try[JMXConnector] =
    () => MBeanConnection(args, 0, 5)

  val Bundles = BundlesService.newInvoker(connector)

  val Config = ConfigService.newInvoker(connector)

  val Features = FeaturesService.newInvoker(connector)

  val System = SystemService.newInvoker(connector)

}

class ExtendedKarafJMXClient(val args: ContainerArgs) extends KarafJMXClient {

  import wav.devtools.karaf.mbeans.MBeanExtensions._

  def deployFeature(repo: URI, name: String, version: String): Try[Unit] =
    Features { s =>
      if (!s.repoRefresh(repo.toString)) sys.error("Couldn't add repository, " + repo)
      if (!s.install(name, version, false)) sys.error("Couldn't install project feature")
    }

  def undeployFeature(repo: URI, name: String, version: String): Try[Unit] =
    Features { s =>
      if (!s.uninstall(name, version)) sys.error("Couldn't uninstall project feature")
      if (!s.repoRemove(repo.toString)) sys.error("Couldn't remove repository, " + repo)
    }

  def updateBundle(bundle: mbeans.Bundle): Try[Unit] =
    Bundles { s =>
      val original = {
        val result = s.bundles.find(_.name == bundle.name)
        if (result.isEmpty) sys.error("Couldn't find the project bundle, " + bundle.name)
        result.get
      }
      s.update(original.bundleId.toString)
      if (bundle.state == BundleState.Active) {
        s.start(original.bundleId.toString)
      }
      val updated = {
        val result = s.bundles.find(_.name == bundle.name)
        if (result.isEmpty) sys.error("Couldn't find the project bundle, " + bundle.name)
        result.get
      }
      if (!BundleState.inState(bundle.state, updated.state))
        sys.error(s"Couldn't resolve the project bundle ${bundle.name}, state: ${updated.state}, expected: ${bundle.name}")
    }
  
}


