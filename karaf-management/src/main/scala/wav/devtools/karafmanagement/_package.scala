package wav.devtools

import javax.management.MBeanServerConnection

package object karafmanagement {
	
	implicit class RichMBeanConnection(val mbsc: MBeanServerConnection) {
		val services = MBeanServices(mbsc)
	}

}