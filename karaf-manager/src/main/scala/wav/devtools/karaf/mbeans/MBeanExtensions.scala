package wav.devtools.karaf.mbeans

import java.io.IOException
import java.net.ServerSocket
import javax.management.openmbean.{CompositeData, TabularData}
import collection.JavaConversions._

import org.apache.karaf

object MBeanExtensions {

  private implicit class RichCompositeData(data: CompositeData) {
    def contains(key: String): Boolean =
      data.containsKey(key)

    def getValue[T](key: String): Option[T] =
      if (contains(key)) Some(data.get(key).asInstanceOf[T]) else None
  }

  private def get(table: TabularData, keys: AnyRef*): Option[CompositeData] =
    Option(table.get(keys.toArray))

  private implicit def boolOpt2Bool(opt: Option[Boolean]): Boolean =
    opt.exists(identity)

  private implicit def dataOpt2Bool(opt: Option[CompositeData]): Boolean =
    opt.isDefined

  implicit class RichFeaturesServiceMBean(mbean: karaf.features.management.FeaturesServiceMBean) {

    private def repos(uri: String): Option[CompositeData] =
      get(mbean.getRepositories, uri)

    private def features(name: String, version: String): Option[CompositeData] =
      get(mbean.getFeatures, name, version)

    def installed(name: String, version: String): Option[Boolean] = {
      for {
        f <- features(name,version)
        installed <- f.getValue[Boolean]("Installed")
      } yield installed
    }

    def repoRemove(uri: String): Boolean =
      if (repos(uri)) {
        mbean.removeRepository(uri)
        !repos(uri)
      } else true

    def repoRefresh(uri: String): Boolean = {
      if (!repos(uri)) {
        mbean.addRepository(uri)
        repos(uri)
      } else {
        mbean.refreshRepository(uri)
        repos(uri)
      }
    }

    def install(name: String, version: String, start: Boolean = false): Boolean = {
      if (!installed(name,version)) mbean.installFeature(name, version, false, !start)
      installed(name, version)
    }

    def uninstall(name: String, version: String): Boolean = {
      if (installed(name,version)) {

        mbean.uninstallFeature(name)
      }
      !installed(name,version)
    }

    def bundleUrls(featureName: String, version: String): Set[String] = {
      val result = for {
          table <- features(featureName, version)
          names <- table.getValue[Array[String]]("Bundles")
        } yield names
      result.getOrElse(Array()).toSet
    }

  }

  implicit class RichBundlesMBean(mbean: karaf.bundle.core.BundlesMBean) {

    private def bundles(id: Long): Option[CompositeData] =
      get(mbean.getBundles, id.asInstanceOf[Object])

    def bundles: Set[Bundle] = {
      var bundles = for (entry <- mbean.getBundles.values.iterator) yield toBundle(entry.asInstanceOf[CompositeData])
      bundles.flatten.toSet
    }

    def installBundle(url: String, start: Boolean = false): Option[Bundle] =
      bundles(mbean.install(url, start)).flatMap(toBundle)

    private def toBundle(data: CompositeData): Option[Bundle] =
      for {
        id <- data.getValue[Long]("ID")
        name <- data.getValue[String]("Name")
        version <- data.getValue[String]("Version")
        state <- data.getValue[String]("State")
      } yield Bundle(id.toInt, name, version, BundleState.withName(state))

  }

}