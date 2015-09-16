package wav.devtools.karaf.manager

import java.io.File
import javax.management.remote.JMXConnector
import org.scalatest.Spec
import wav.devtools.karaf.mbeans.{MBeanServices, MBeanConnection}

class ContainerSuite extends Spec {

  def `start and stop a karaf container`(): Unit = {
    val `karaf.base` = System.getProperty("karaf.base", "NOT_SET")
    assert(`karaf.base` != "NOT_SET", "karaf.base system property not set.")
    assert(new File(`karaf.base`).isDirectory(), `karaf.base` + " not found")
    val config = KarafContainer.configuration(`karaf.base`)
    val container = new KarafContainer(config)

    def getName(): Unit = {
      var connection: JMXConnector = null
      try
      {
        container.start()
        Thread.sleep(1000)
        require(container.isAlive, "inspect container.log")
        connection = MBeanConnection(config.containerArgs, 0, 5).get
        val system = MBeanServices(connection).System.get
        assert(system.getName == "root")
        println("container started")
      }
      finally
      {
        if (container != null) connection.close()
        container.stop()
        println("container stop")
      }
    }

    getName
    getName
  }

}
