package wav.devtools.karaf.manager

import org.scalatest.Sequential

class StepsSuite extends Sequential(
  new ContainerSpec,
  new DeploymentSpec
)
