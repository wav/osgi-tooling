package wav.devtools.karaf

import javax.management.MBeanServerConnection

package object mbeans {
	
	implicit class RichMBeanConnection(val mbsc: MBeanServerConnection) {
		val services = MBeanServices(mbsc)
	}

}