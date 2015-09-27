package wav.devtools.karaf

import java.io.File

import org.apache.commons.io.FileUtils

package object packaging {

  import Util.MavenRepo

  val localRepo =   MavenRepo("m2 local", new File(FileUtils.getUserDirectory, ".m2/settings.xml").toURI.toString, true)
  val jcenterRepo = MavenRepo("jcenter", "https://jcenter.bintray.com/", false)
  val centralRepo = MavenRepo("central", "https://repo1.maven.org/maven2/", false)

}
