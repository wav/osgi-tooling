package wav.devtools.sbt.karaf

import sbt._
import wav.devtools.karaf.packaging.FeaturesXml
import FeaturesXml._

import scala.util.{Try, Success, Failure}

package object packaging {

  private [packaging] type SbtTask[T] = Def.Initialize[Task[T]]

  private [packaging] def toDep(dep: (String, String)): Dependency =
    Try(dep._2.versionRange) match {
      case Success(vr) => Dependency(dep._1, vr, true, true)
      case Failure(ex) =>
        sys.error(s"The referenced feature ${dep._1} does not have a valid version identifier: " + ex.getMessage)
        ???
    }

  private[devtools] def nullToEmpty(s: String): String = if (s == null) "" else s

}
