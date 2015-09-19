package wav.devtools.karaf.manager

import java.io.File
import javax.management.remote.JMXConnector
import org.scalatest.Spec
import wav.devtools.karaf.mbeans.{MBeanServices, MBeanConnection}

import scala.util.Try

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
        if (container != null) container.stop()
        println("container stopped")
      }
    }
    println("Starting up the container.")
    getName
    println("Checking that the container cannot be reached")
    assert(Try {
      val connection = MBeanConnection(config.containerArgs, 0, 5).get
      MBeanServices(connection).System.get.getName
    }.isFailure)
    println("Container cannot be reached! :)")
    println("Starting up the container again.")
    getName
  }

}
