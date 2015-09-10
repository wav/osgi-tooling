package wav.devtools.karaf.mbeans

case class Bundle(bundleId: Int, name: String, version: String, state: BundleState.Value)

case class ContainerArgs(serviceUrl: String, user: String, pass: String)

case class Instance(name: String, sshPort: Int, rmiRegistryPort: Int, rmiServerPort: Int, state: String, location: String, pid: Int)

case class CreateInstanceArgs(name: String, location: String,
  sshPort: Int = 0,
  rmiRegistryPort: Int = 0,
  rmiServerPort: Int = 0,
  javaOpts: String = null,
  features: Array[String] = Array.empty,
  repositories: Array[String] = Array.empty)

object ServiceUrl {

  val Pattern = """service:jmx:rmi:///jndi/rmi://(.*):(.*)/(.*)""".r

  def unapply(url: String): Option[ServiceUrl] =
    url match {
      case Pattern(host, port, instanceName) =>
        Some(ServiceUrl(host, port.toInt, instanceName))
      case _ => None
    }
}

case class ServiceUrl(host: String, port: Int, instanceName: String) {
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