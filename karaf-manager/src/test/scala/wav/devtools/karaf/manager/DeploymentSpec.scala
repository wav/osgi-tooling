package wav.devtools.karaf.manager

import java.io.File

import org.scalatest.{BeforeAndAfter, Spec}
import wav.devtools.karaf.packaging.Util

import wav.devtools.karaf.mbeans.{BundleState, Bundle}

class DeploymentSpec extends Spec with BeforeAndAfter {

  var container: KarafContainer = null
  var client: ExtendedKarafJMXClient = null

  before {
    container = KarafContainer.Default
    println(s"Starting up the container.")
    container.start()
    client = new ExtendedKarafJMXClient(container.config.containerArgs)
  }

  def `can (un)deploy a feature and refresh a bundle`(): Unit = {
    val BUNDLE_VERSION = "0.1.0.SNAPSHOT"
    val BUNDLE_NAME = "default.refresh.bundle"
    val FEATURE_NAME = "refresh-bundle"
    Util.withTemporaryDirectory { tempDir =>
      println(s"Copying resources from classpath")
      val bundleTarget = new File(tempDir, s"$FEATURE_NAME.jar")
      Util.writeResourceToFile(classOf[DeploymentSpec].getResourceAsStream(s"/$FEATURE_NAME.jar"), bundleTarget)
      assert(bundleTarget.exists())
      val featuresTarget = new File(tempDir, s"$FEATURE_NAME-features.xml")
      Util.writeStringResourceToFile(
        classOf[DeploymentSpec].getResourceAsStream(s"/$FEATURE_NAME-features.xml"),
        featuresTarget,
        _.replace(s"@$FEATURE_NAME@", bundleTarget.toURI.toString))
      assert(featuresTarget.exists())
      val repo = featuresTarget.toURI.toString
      println(s"Starting feature $FEATURE_NAME in $repo")
      client.startFeature(repo, FEATURE_NAME, BUNDLE_VERSION).get
      val bundle = Bundle(-1, BUNDLE_NAME, BUNDLE_VERSION, BundleState.Active)
      println(s"Updating bundle $BUNDLE_NAME")
      client.updateBundle(bundle).get
      println(s"Uninstalling and removing repository")
      client.Features(_.removeRepository(repo, true)).get
    }
  }

  after {
    container.stop()
    println(s"container stopped")
  }

}