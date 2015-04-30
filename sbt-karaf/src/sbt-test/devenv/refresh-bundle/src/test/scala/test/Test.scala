package test

/**
 * https://ops4j1.jira.com/wiki/display/PAXEXAM4/Karaf+Container
 */

import collection.JavaConversions._

import javax.inject.Inject
import org.junit.Test
import org.junit.runner.RunWith
import org.ops4j.pax.exam.Configuration
import org.ops4j.pax.exam.{Option => PaxOption}
import org.ops4j.pax.exam.junit.PaxExam
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy
import org.ops4j.pax.exam.spi.reactors.PerMethod
import java.io.File
import org.osgi.framework.{Bundle, BundleContext}

@RunWith(classOf[PaxExam])
@ExamReactorStrategy(Array(classOf[PerMethod]))
class SampleTest {

  @Inject var context: BundleContext = _

  @Configuration
  def config(): Array[PaxOption] = {
    import org.ops4j.pax.exam.CoreOptions._
    import org.ops4j.pax.exam.karaf.options._, KarafDistributionOption._

   val karafVersion = "4.0.0.M2"

    val karafUrl = maven()
      .groupId("org.apache.karaf")
      .artifactId("apache-karaf")
      .versionAsInProject()
//      .version(karafVersion)
      .`type`("tar.gz")
    val karafStandardRepo = maven()
      .groupId("org.apache.karaf.features")
      .artifactId("standard")
      .classifier("features")
      .version(karafVersion)
      .`type`("xml")

    val osgiBundle = bundle("mvn:org.osgi/org.osgi.core/6.0.0") // .versionAsInProject()
    val scalaBundle = bundle("mvn:org.scala-lang/scala-library/2.11.6") // .versionAsInProject()

    options(
      editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository", "~/.m2/repository"),
      editConfigurationFilePut("etc/org.apache.felix.eventadmin.impl.EventAdmin.cfg", "org.apache.felix.eventadmin.IgnoreTimeout", ""),
      editConfigurationFilePut("etc/org.apache.felix.eventadmin.impl.EventAdmin.cfg", "org.apache.felix.eventadmin.IgnoreTopic", ""),
//      debugConfiguration("5005", true),
      karafDistributionConfiguration()
        .frameworkUrl(karafUrl)
        .unpackDirectory(new File("target/exam"))
        .useDeployFolder(false),
      logLevel(LogLevelOption.LogLevel.WARN),
      provision(scalaBundle),
      keepRuntimeFolder(),
      keepCaches(),
      features(karafStandardRepo, "eventadmin"),
      junitBundles()
    )
  }

  import org.junit.Assert._

  @Test
  def truthy(): Unit = {
    assertTrue(true)
  }
}