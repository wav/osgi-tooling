package wav.devtools.karaf.manager

import java.io.File

import org.scalatest.{BeforeAndAfter, Spec}
import java.nio.file.{Paths, Files}

import wav.devtools.karaf.mbeans.{BundleState, Bundle}

class DeploymentSpec extends Spec with BeforeAndAfter {

  var container: KarafContainer = null
  var client: ExtendedKarafJMXClient = null

  before {
    container = KarafContainer.Default
    container.start()
    client = new ExtendedKarafJMXClient(container.config.containerArgs)
  }

  def `can (un)deploy a feature and refresh a bundle`(): Unit = {
    val BUNDLE_VERSION = "0.1.0.SNAPSHOT"
    val BUNDLE_NAME = "default.refresh.bundle"
    val FEATURE_NAME = "refresh-bundle"
    val tempDir = Files.createTempDirectory(s"DeploymentSuite_$FEATURE_NAME")
    val bundleJar = classOf[DeploymentSpec].getResourceAsStream(s"/$FEATURE_NAME.jar")
    val bundleJarFile = new File(tempDir.toFile, s"$FEATURE_NAME.jar")
    Files.copy(bundleJar, Paths.get(bundleJarFile.toURI))
    val featuresFile = new File(tempDir.toFile, s"$FEATURE_NAME-features.xml")
    val featuresXml = io.Source.fromInputStream(classOf[DeploymentSpec].getResourceAsStream(s"/$FEATURE_NAME-features.xml"))
      .getLines.mkString.replace(s"@$FEATURE_NAME@", bundleJarFile.toURI.toString)
    import java.io._
    val pw = new PrintWriter(featuresFile)
    pw.write(featuresXml)
    pw.close
    assert(client.deployFeature(featuresFile.toURI, FEATURE_NAME, BUNDLE_VERSION).isSuccess)
    val bundle = Bundle(-1, BUNDLE_NAME, BUNDLE_VERSION, BundleState.Active)
    assert(client.updateBundle(bundle).isSuccess)
    assert(client.undeployFeature(featuresFile.toURI, FEATURE_NAME, BUNDLE_VERSION).isSuccess)
  }

  after {
    container.stop()
  }

}