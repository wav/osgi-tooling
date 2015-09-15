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
	  		JMX.newMBeanProxy(
	    		mbsc,
	    		names.toSeq(0).getObjectName,
	    		clazz,
	    		true)
	  	}

}

case class MBeanServices(connector: JMXConnector) {
	import org.apache.karaf

	private def get[T](query: String, clazz: Class[T]): Try[T] = 
		MBeanConnection.get[T](connector, query, clazz)

	def Config = get("org.apache.karaf:type=config,name=*", classOf[karaf.config.core.ConfigMBean])
	def Bundles = get("org.apache.karaf:type=bundle,name=*", classOf[karaf.bundle.core.BundlesMBean])
	def FeaturesService = get("org.apache.karaf:type=feature,name=*", classOf[karaf.features.management.FeaturesServiceMBean])
	def Instances = get("org.apache.karaf:type=instance,name=*", classOf[karaf.instance.core.InstancesMBean])
  def System = get("org.apache.karaf:type=system,name=*", classOf[karaf.system.management.SystemMBean])

}