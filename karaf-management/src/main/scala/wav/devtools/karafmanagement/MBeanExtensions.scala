package wav.devtools.karafmanagement

import java.io.IOException
import java.net.ServerSocket
import javax.management.openmbean.{CompositeData, TabularData}

import org.apache.karaf

object MBeanExtensions {

  implicit class RichTabularData(table: TabularData) {
    def contains(keys: Object*): Boolean =
      table.containsKey(keys.toArray)

    def get(keys: Object*): Option[CompositeData] =
      if (contains(keys)) Some(table.get(keys.toArray)) else None
  }

  implicit class RichCompositeData(data: CompositeData) {
    def contains(key: String): Boolean =
      data.containsKey(key)

    def getValue[T](key: String): Option[T] =
      if (contains(key)) Some(data.get(key).asInstanceOf[T]) else None
  }

  private implicit def boolOpt2Bool(opt: Option[Boolean]): Boolean =
    opt.exists(identity)

  implicit class RichFeaturesServiceMBean(mbean: karaf.features.management.FeaturesServiceMBean) {

    def installed(name: String, version: Option[String] = None): Option[Boolean] = {
      lazy val fs = mbean.getFeatures
      for {
        f <- if (version.isDefined) fs.get(name, version.get) else fs.get(name)
        installed <- f.getValue[Boolean]("Installed")
      } yield installed
    }

    def repoRemove(uri: String): Boolean =
      if (mbean.getRepositories.contains(uri)) {
        mbean.removeRepository(uri)
        mbean.getRepositories.contains(uri)
      } else false

    def repoAdd(uri: String): Boolean =
      if (!mbean.getRepositories.contains(uri)) {
        mbean.addRepository(uri)
        mbean.getRepositories.contains(uri)
      } else true

    def repoRefresh(uri: String): Boolean = {
      if (!mbean.getRepositories.contains(uri)) {
        mbean.addRepository(uri)
        return mbean.getRepositories.contains(uri)
      } else {
        mbean.refreshRepository(uri)
        mbean.getRepositories.contains(uri)
      }
    }

    def install(name: String, version: String, start: Boolean = false): Boolean = {
      if (!installed(name, Some(version)) && installed(name)) {
        uninstall(name)
        if (installed(name)) return false
      }
      mbean.installFeature(name, version, false, !start)
      installed(name, Some(version))
    }

    def uninstall(name: String): Boolean = {
      if (installed(name)) mbean.uninstallFeature(name)
      installed(name)
    }

    def bundleUrls(featureName: String): Set[String] = {
      val result = for {
          table <- mbean.getFeatures.get(featureName)
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

    def getInstance(name: String): Option[Instance] =
      for {
        i <- mbean.getInstances.get(name)
        sshPort <- i.getValue[Int]("SSH Port")
        rmiRegistryPort <- i.getValue[Int]("RMI Registry Port")
        rmiServerPort <- i.getValue[Int]("RMI Server Port")
        state <- i.getValue[String]("State")
        location <- i.getValue[String]("Location")
        pid <- i.getValue[String]("Pid")
      } yield Instance(name, sshPort, rmiRegistryPort, rmiServerPort, state, location, pid)

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

    def bundles: Set[Bundle] = {
      var table = mbean.getBundles
      val keys = table.keySet()
      var list = for (k <- keys.toArray) yield toBundle(table.get(Array(k)))
      list.flatten.toSet
    }

    def installBundle(url: String, start: Boolean = false): Option[Bundle] = {
      val id = mbean.install(url, start)
      mbean.getBundles.get(id.asInstanceOf[Object]).flatMap(toBundle)
    }

    private def toBundle(data: CompositeData): Option[Bundle] =
      for {
        id <- data.getValue[Long]("ID")
        name <- data.getValue[String]("Name")
        version <- data.getValue[String]("Version")
        state <- data.getValue[String]("State")
      } yield Bundle(id.toInt, name, version, state)

  }

}