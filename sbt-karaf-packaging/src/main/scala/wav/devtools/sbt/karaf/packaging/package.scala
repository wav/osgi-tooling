package wav.devtools.sbt.karaf

import sbt._

package object packaging {

  private [packaging] type SbtTask[T] = Def.Initialize[Task[T]]

}
