package wav.devtools.karaf.mbeans

case class Bundle(bundleId: Int, name: String, version: String, state: BundleState.Value)

abstract class ContainerArgs {
  val serviceUrl: String
}

object ContainerArgs {

  val Default = Password(KarafServiceUrl.Default.toString, "karaf", "karaf")

  case class Password(serviceUrl: String, user: String, pass: String) extends ContainerArgs

}

case class Instance(name: String, sshPort: Int, rmiRegistryPort: Int, rmiServerPort: Int, state: String, location: String, pid: Int)

case class CreateInstanceArgs(name: String, location: String,
  sshPort: Int = 0,
  rmiRegistryPort: Int = 0,
  rmiServerPort: Int = 0,
  javaOpts: String = null,
  features: Array[String] = Array.empty,
  repositories: Array[String] = Array.empty)

object KarafServiceUrl {

  val Default = KarafServiceUrl("localhost", 1099, "karaf-root")

  val Pattern = """service:jmx:rmi:///jndi/rmi://(.*):(.*)/(.*)""".r

  def unapply(url: String): Option[KarafServiceUrl] =
    url match {
      case Pattern(host, port, instanceName) =>
        Some(KarafServiceUrl(host, port.toInt, instanceName))
      case _ => None
    }
}

case class KarafServiceUrl(host: String, port: Int, instanceName: String) {
  override def toString: String =
    s"service:jmx:rmi:///jndi/rmi://${host}:${port}/${instanceName}"
}

object BundleState extends Enumeration {
  val Error = Value("Error")
  val Uninstalled = Value("Uninstalled")
  val Installed = Value("Installed")
  val Starting = Value("Starting")
  val Stopping = Value("Starting")
  val Resolved = Value("Resolved")
  val Active = Value("Active")

  private val lifecycle = Seq(Error, Installed, Resolved, Active)

  def inState(expected: BundleState.Value, actual: BundleState.Value): Boolean =
    lifecycle.indexOf(actual) >= lifecycle.indexOf(expected)
}

case class BundleStartArgs(name: String, startState: BundleState.Value = BundleState.Active)