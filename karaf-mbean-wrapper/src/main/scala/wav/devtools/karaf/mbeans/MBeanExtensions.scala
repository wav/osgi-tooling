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

    def repoAdd(uri: String): Boolean =
      if (!repos(uri)) {
        mbean.addRepository(uri)
        repos(uri)
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

  implicit class RichInstancesMBean(mbean: karaf.instance.core.InstancesMBean) {

    private def findFreePort: Option[Int] = {
      var port: Option[Int] = None
      try {
        val server = new ServerSocket(0)
        Some(server.getLocalPort)
        server.close
      } catch {
        case _: IOException =>
      }
      port
    }

    private def instances(name: String): Option[CompositeData] =
      get(mbean.getInstances, name)

    def getInstance(name: String): Option[Instance] = {
      for {
        i <- instances(name)
        sshPort <- i.getValue[Int]("SSH Port")
        rmiRegistryPort <- i.getValue[Int]("RMI Registry Port")
        rmiServerPort <- i.getValue[Int]("RMI Server Port")
        state <- i.getValue[String]("State")
        location <- i.getValue[String]("Location")
        pid <- i.getValue[Int]("Pid")
      } yield Instance(name, sshPort, rmiRegistryPort, rmiServerPort, state, location, pid)
    }

    def create(args: CreateInstanceArgs): Option[Instance] = {
      mbean.createInstance(args.name, args.sshPort, args.rmiRegistryPort, args.rmiServerPort, args.location, args.javaOpts, args.features.mkString(""), args.repositories.mkString(""))
      getInstance(args.name)
    }

    def clone(
      name: String, cloneName: String, location: String,
      sshPort: Int = 0,
      rmiRegistryPort: Int = 0,
      rmiServerPort: Int = 0,
      javaOpts: String = null): Option[Instance] = {
      mbean.cloneInstance(name, cloneName, sshPort, rmiRegistryPort, rmiServerPort, location, javaOpts)
      getInstance(cloneName)
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