package wav.devtools.sbt.karaf

import sbt._

package object packaging {

  private [packaging] type SbtTask[T] = Def.Initialize[Task[T]]

  def FeatureID(o: String, n: String, v: String, a: Option[String] = None) =
    ModuleID(o, n, v, explicitArtifacts = Seq(Artifact(a getOrElse s"$n", "xml", "xml", "features")))

}
