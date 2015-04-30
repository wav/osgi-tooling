package wav.devtools.karaf.mbeans

import java.util.HashMap

import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import javax.management.JMX
import javax.management.ObjectInstance
import javax.management.ObjectName

import collection.JavaConversions.asScalaSet
import util.Try

object MBeanConnection {

  @throws(classOf[Exception])
  def apply(args: ContainerArgs): MBeanServerConnection =
		args match {
			case ContainerArgs.Password(serviceUrl, user, password) =>
				val environment = new HashMap[String, Array[String]]
				environment.put(JMXConnector.CREDENTIALS, Array[String](user, password))
				JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl), environment).getMBeanServerConnection
		}

  @throws(classOf[Exception])
	def get[T](mbsc: MBeanServerConnection, query: String, clazz: Class[T]): Try[T] =
		Try[T] {
			val names: Set[ObjectInstance] = mbsc.queryMBeans(new ObjectName(query), null).toSet
			require(names.toSeq.length > 0, s"MBean not found, query: $query")
	  		JMX.newMBeanProxy(
	    		mbsc,
	    		names.toSeq(0).getObjectName,
	    		clazz,
	    		true)
	  	}

}

case class MBeanServices(mbsc: MBeanServerConnection) {
	import org.apache.karaf

	private def get[T](query: String, clazz: Class[T]): Try[T] = 
		MBeanConnection.get[T](mbsc, query, clazz)

	def Config = get("org.apache.karaf:type=config,name=*", classOf[karaf.config.core.ConfigMBean])
	def Bundles = get("org.apache.karaf:type=bundle,name=*", classOf[karaf.bundle.core.BundlesMBean])
	def FeaturesService = get("org.apache.karaf:type=feature,name=*", classOf[karaf.features.management.FeaturesServiceMBean])
	def Instances = get("org.apache.karaf:type=instance,name=*", classOf[karaf.instance.core.InstancesMBean])

}