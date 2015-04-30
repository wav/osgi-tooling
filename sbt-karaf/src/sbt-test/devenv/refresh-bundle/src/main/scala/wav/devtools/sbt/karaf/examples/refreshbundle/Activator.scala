package wav.devtools.sbt.karaf.examples.refreshbundle

import org.osgi.framework.{BundleContext, BundleActivator}
import wav.devtools.sbt.karaf.examples.refreshbundle.impl.PrinterService

class Activator extends impl.PrinterService with BundleActivator {

  @throws[Exception]
  def start(context: BundleContext): Unit = {
    val svc = new PrinterService
    context.
      registerService(classOf[PrinterService].getName, svc, null)
    svc.resume
  }

  @throws[Exception]
  def stop(context: BundleContext): Unit = {
    val serviceReference = context.
      getServiceReference(classOf[PrinterService].getName)
    val service = context.getService(serviceReference).asInstanceOf[PrinterService]
    service.pause
    context.ungetService(serviceReference)
  }

}
