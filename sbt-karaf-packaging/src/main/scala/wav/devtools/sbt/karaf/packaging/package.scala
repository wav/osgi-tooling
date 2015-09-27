package wav.devtools.sbt.karaf

import org.osgi.framework.Version
import sbt._
import wav.devtools.karaf.packaging.FeaturesXml
import FeaturesXml._

package object packaging {

  private [packaging] type SbtTask[T] = Def.Initialize[Task[T]]

  private [packaging] def toDep(t: (String, String)): Dependency =
    Dependency(t._1, {
      require(t._2 != null && t._2.trim.length > 0, s"The referenced feature ${t._1} does not have a valid version identifier, use `*` to specify any version.")
      if (t._2 == "*") Version.emptyVersion.toString() else t._2
    })

  private[devtools] def nullToEmpty(s: String): String = if (s == null) "" else s

}
