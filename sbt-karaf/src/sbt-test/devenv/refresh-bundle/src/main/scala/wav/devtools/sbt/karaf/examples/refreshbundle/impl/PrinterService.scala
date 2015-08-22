package wav.devtools.sbt.karaf.examples.refreshbundle.impl

import wav.devtools.sbt.karaf.examples.{refreshbundle => api}

import org.slf4j.LoggerFactory

class PrinterService extends api.PrinterService {

  private val logger = LoggerFactory.getLogger(classOf[PrinterService])

  private var _active = false

  def pause(): Unit = {
    logger.info("PrinterService has been paused")
    _active = false
  }

  def resume(): Unit = {
    _active = true
    logger.info("PrinterService has started")
    while(_active) {
      logger.info("PrinterService is active")
      Thread.sleep(1000)
    }
  }

}
