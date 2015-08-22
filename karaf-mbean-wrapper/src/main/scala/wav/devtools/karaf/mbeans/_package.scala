package wav.devtools.karaf

import javax.management.remote.JMXConnector

package object mbeans {
	
	implicit class RichMBeanConnection(val connector: JMXConnector) {
		val services = MBeanServices(connector)
	}

}