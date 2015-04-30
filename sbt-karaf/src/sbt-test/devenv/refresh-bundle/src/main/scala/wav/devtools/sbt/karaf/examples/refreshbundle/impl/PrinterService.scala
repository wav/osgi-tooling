package wav.devtools.sbt.karaf.examples.refreshbundle.impl

import wav.devtools.sbt.karaf.examples.{refreshbundle => api}

class PrinterService extends api.PrinterSerice {

  private var _active = false

  def pause(): Unit =
    _active = false

  def resume(): Unit = {
    _active = true
    var n = 0
    while(_active) {
      println(s"PrinterService active for $n seconds")
      n += 1
      Thread.sleep(1000)
    }
  }

}
