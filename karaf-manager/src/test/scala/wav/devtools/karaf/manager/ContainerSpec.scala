package wav.devtools.karaf.manager

import org.scalatest.Spec

import scala.util.Success

class ContainerSpec extends Spec {

  def `start and stop a karaf container`(): Unit = {
    val container = KarafContainer.Default
    val C = new ExtendedKarafJMXClient(container.config.containerArgs)

    def getName(): Unit = {
      try
      {
        container.start()
        Thread.sleep(1000)
        require(container.isAlive, "inspect container.log")
        assert(C.System(_.getName) == Success("root"))
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
    C.System(_.getName)
    println("Container cannot be reached! :)")
    println("Starting up the container again.")
    getName
  }

}
