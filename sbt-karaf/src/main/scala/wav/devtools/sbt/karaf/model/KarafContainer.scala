package wav.devtools.sbt.karaf.model

case class BundleStartArgs(name: String, startState: String = "Started") {
  def Started = copy(startState = "Started")
  def Resolved = copy(startState = "Resolved")
}