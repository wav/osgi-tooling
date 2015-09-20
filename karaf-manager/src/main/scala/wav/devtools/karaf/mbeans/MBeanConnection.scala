package wav.devtools.karaf.mbeans

import java.io.IOException
import java.rmi.NoSuchObjectException
import java.util.HashMap
import javax.management.remote.{JMXConnector, JMXConnectorFactory, JMXServiceURL}
import javax.management.{JMX, ObjectInstance, ObjectName}
import javax.naming.{ServiceUnavailableException, CommunicationException}
import scala.collection.JavaConversions.asScalaSet
import scala.util.{Failure, Try}

object MBeanConnection {

  @throws(classOf[Exception])
  def apply(creds: ContainerArgs, retries: Int = 0, maxRetries: Int = 10): Try[JMXConnector] =
    Try {
      val environment = new HashMap[String, Array[String]]
      environment.put(JMXConnector.CREDENTIALS, Array[String](creds.user, creds.pass))
      JMXConnectorFactory.connect(new JMXServiceURL(creds.serviceUrl), environment)
    }
      .recoverWith {
      // retry if the server doesn't appear to be ready.
      case nobex: NoSuchObjectException if retries <= maxRetries =>
        Thread.sleep(1000 * retries)
        println("Karaf service not ready, retrying ...")
        apply(creds, retries + 1, maxRetries)
      case ioex: IOException =>
        ioex.getCause() match {
          case suex: ServiceUnavailableException if retries <= maxRetries =>
            Thread.sleep(1000 * retries)
            println("Karaf server not available, retrying ...")
            apply(creds, retries + 1, maxRetries)
          case commex: CommunicationException =>
            commex.getRootCause() match {
              case nobex: NoSuchObjectException if retries <= maxRetries =>
                Thread.sleep(1000 * retries)
                println("Karaf service not available, retrying ...")
                apply(creds, retries + 1, maxRetries)
              case _ => Failure(ioex)
            }
          case _ => Failure(ioex)
        }
    }

  @throws(classOf[Exception])
  def get[T](connector: JMXConnector, query: String, clazz: Class[T], retries: Int = 0): Try[T] =
    Try[T] {
      val mbsc = connector.getMBeanServerConnection
      val names: Set[ObjectInstance] = mbsc.queryMBeans(new ObjectName(query), null).toSet
      require(names.toSeq.length > 0, s"MBean not found, query: $query")
      JMX.newMBeanProxy(mbsc, names.toSeq(0).getObjectName, clazz, true)
    }

}

class MBeanService[T](val mbeanQuery: String, val clazz: Class[T]) {
  def newInvoker(connector: () => Try[JMXConnector]): MBeanInvoker[T] =
    new MBeanInvoker(this,connector)
}

class MBeanInvoker[T](s: MBeanService[T], connector: () => Try[JMXConnector]) {
  def apply[R](f: T => R): Try[R] =
    for {
      c <- connector()
      mbean <- MBeanConnection.get[T](c, s.mbeanQuery, s.clazz)
      result = f(mbean)
      _ = c.close()
    } yield result
}

case class Bundle(bundleId: Int, name: String, version: String, state: BundleState.Value)

case class ContainerArgs(serviceUrl: String, user: String, pass: String)

object ServiceUrl {

  val Pattern = """service:jmx:rmi:///jndi/rmi://(.*):(.*)/(.*)""".r

  def unapply(url: String): Option[ServiceUrl] =
    url match {
      case Pattern(host, port, instanceName) =>
        Some(ServiceUrl(host, port.toInt, instanceName))
      case _ => None
    }
}

case class ServiceUrl(host: String, port: Int, instanceName: String) {
  override def toString: String =
    s"service:jmx:rmi:///jndi/rmi://${host}:${port}/${instanceName}"
}

object BundleState extends Enumeration {
  val Error = Value("Error")
  val Uninstalled = Value("Uninstalled")
  val Installed = Value("Installed")
  val Starting = Value("Starting")
  val Stopping = Value("Starting")
  val Resolved = Value("Resolved")
  val Active = Value("Active")

  private val lifecycle = Seq(Error, Installed, Resolved, Active)

  def inState(expected: BundleState.Value, actual: BundleState.Value): Boolean =
    lifecycle.indexOf(actual) >= lifecycle.indexOf(expected)
}