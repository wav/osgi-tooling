package wav.devtools.sbt.karaf.examples.refreshbundle

import org.osgi.framework.{ServiceRegistration, BundleContext, BundleActivator}

class Activator extends BundleActivator {

  private val serviceName = "wav.devtools.sbt.karaf.examples.refreshbundle.PrinterService"

  private var registration: ServiceRegistration[PrinterService] = null
  private var thread: Thread = null

  @throws[Exception]
  def start(context: BundleContext): Unit = {
    println(s"Registering $serviceName")
    val svc = new impl.PrinterService
    if (registration == null) {
      registration = context
        .registerService(classOf[PrinterService].getName, svc, null)
        .asInstanceOf[ServiceRegistration[PrinterService]]
      println(s"Registered $serviceName")
    }
    else println(s"Already registered $serviceName")
    thread = new Thread {
      override def run(): Unit = svc.resume
    }
    thread.start()
  }

  @throws[Exception]
  def stop(context: BundleContext): Unit = {
//    val serviceReference = context.
//      getServiceReference(classOf[PrinterService].getName)
//    val service = context.getService(serviceReference).asInstanceOf[PrinterService]
//    service.pause
//    context.ungetService(serviceReference)
    if (thread != null) {
      thread.interrupt()
      thread = null
    }
    if (registration != null) {
      registration.unregister()
      registration = null
      println(s"Unregistered $serviceName")
    }
  }

}
