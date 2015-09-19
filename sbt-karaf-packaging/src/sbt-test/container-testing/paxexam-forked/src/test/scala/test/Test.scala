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
import org.ops4j.pax.exam.CoreOptions._
import org.ops4j.pax.exam.karaf.options._, KarafDistributionOption._
import org.junit.Assert._
import java.net.URI

@RunWith(classOf[PaxExam])
@ExamReactorStrategy(Array(classOf[PerMethod]))
class SampleTest {

  @Inject var context: BundleContext = _

  @Configuration
  def config(): Array[PaxOption] = {

    // val karafUrl = maven()
    //   .groupId("org.apache.karaf")
    //   .artifactId("apache-karaf-minimal")
    //   .versionAsInProject()
    //   .`type`("tar.gz")

    val karafStandardRepo = maven()
      .groupId("org.apache.karaf.features")
      .artifactId("standard")
      .classifier("features")
      .versionAsInProject()
      .`type`("xml")

    val scalaBundle = mavenBundle()
      .groupId("org.scala-lang")
      .artifactId("scala-library")
      .versionAsInProject()

    val karafDistOption = {
      val file = System.getProperty("karaf.distribution", "NOT_SET")
      karafDistributionConfiguration().frameworkUrl(file)
        .unpackDirectory(new File("target", "exam"))
        .useDeployFolder(false)
    }

    // takes 33s
    options(
      // KarafDistributionOption.debugConfiguration("5005", true),
      editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository", "~/.m2/repository"),
      karafDistOption,
      configureConsole().ignoreLocalConsole(),
      configureConsole().ignoreRemoteShell(),
      provision(scalaBundle),
      keepCaches(),
      junitBundles()
    )
  }

  @Test
  def truthy(): Unit = {
    assertTrue(true)
  }
}