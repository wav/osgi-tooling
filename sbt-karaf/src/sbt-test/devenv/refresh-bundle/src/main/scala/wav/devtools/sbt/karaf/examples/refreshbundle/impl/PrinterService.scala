package wav.devtools.sbt.karaf.examples.refreshbundle.impl

import wav.devtools.sbt.karaf.examples.{refreshbundle => api}

import org.slf4j.LoggerFactory
import org.json.JSONObject
import collection.JavaConversions._

class PrinterService extends api.PrinterService {

  private val logger = LoggerFactory.getLogger(classOf[PrinterService])

  private var _active = false

  private def logMessage(ms: (String, String)*) {
    val m = mapAsJavaMap(Map(ms: _*))
    logger.info(new JSONObject(m).toString())
  }

  def pause(): Unit = {
    _active = false
    logMessage("controller event" -> "paused")
  }

  def resume(): Unit = {
    _active = true
    logMessage("controller event" -> "resumed")
    while(_active) {
      logMessage("controller notification" -> "alive")
      Thread.sleep(5000)
    }
  }

}
