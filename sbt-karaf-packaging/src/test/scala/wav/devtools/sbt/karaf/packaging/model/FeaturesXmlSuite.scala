package wav.devtools.sbt.karaf.packaging.model

import java.io.File

import org.scalatest.Spec
import wav.devtools.sbt.karaf.packaging.{Util => ThisUtil, Resolution}
import wav.devtools.sbt.karaf.packaging.model._, FeaturesXml._

class FeaturesXmlSuite extends Spec {

  import sbt._

  val standardID = "org.apache.karaf.features" % "standard" % "4.0.0" classifier("features")
  val enterpriseID = "org.apache.karaf.features" % "enterprise" % "4.0.0" classifier("features")
  val paxWebID = "org.ops4j.pax.web" % "pax-web-features" % "4.1.4" classifier("features")

  val repoIDs = Map[sbt.ModuleID, String](
    standardID -> s"/standard-4.0.0-features.xml",
    enterpriseID -> s"/enterprise-4.0.0-features.xml",
    paxWebID -> "/pax-web-features-4.1.4-features.xml"
  )

  def getRepo(m: sbt.ModuleID): Option[FeatureRepository] =
    repoIDs.get(m).map { path =>
      import sbt.IO
      val stream = getClass.getResourceAsStream(path)
      IO.withTemporaryFile("featuresRepository", new File(path).getName) { f =>
        IO.write(f, IO.readStream(stream))
        val data = FeaturesArtifact(
          m,
          m.explicitArtifacts.head,
          Some(f),
          None)
        val repo = data.toRepository
        assert(repo.isDefined)
        repo.get
      }
    }

  val jolokia13 = Dependency("jolokia","1.3.0",false,false)

  def `should read valid feature files`(): Unit = {
    val Some(fr) = getRepo(standardID)
    val paxRepo = Repository("mvn:org.ops4j.pax.web/pax-web-features/4.1.4/xml/features")
    assert(fr.featuresXml.elems.contains(paxRepo))
    val jolokia = feature("jolokia","1.3.0",Set(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"), Dependency("http",None)))
    val Some(selection) = fr.featuresXml.elems.collectFirst {
      case f @ Feature(name, _, _, Some(_)) if name == "jolokia" => f
    }
    assert(jolokia.name == selection.name)
    assert(jolokia.version == selection.version)
    assert(jolokia.deps.contains(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0")))
    assert(jolokia.deps.contains(Dependency("http",None)))
  }

  def `should identify feature that is defined but not referenced`(): Unit = {
    val Some(fr) = getRepo(standardID)
    fr.features.exists(Resolution.satisfies(jolokia13, _))
  }

  def `should identify a feature dependency that is not defined but referenced`(): Unit = {
    val Some(fr) = getRepo(standardID)
    fr.features.exists(Resolution.satisfies(Dependency("pax-http"), _))
  }

  def `should identify a bundle dependency`(): Unit = {
    val Some(fr) = getRepo(standardID)
    fr.features.flatMap(_.deps).contains(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"))
  }

  def `parses a maven url`(): Unit = {
    val mvnUrl = MavenUrl("g","a.b", "1", Some("xml"), Some("features"))
    val MavenUrl(result) = mvnUrl.toString
    assert(mvnUrl.toString == result.toString)
    assert(mvnUrl.classifer == result.classifer)
    assert(mvnUrl == result)
  }

  def `features versions and constraints are comparable`(): Unit = {
    val jolokia13vr = Dependency("jolokia","[1.3,1.4)", false, false)
    assert(jolokia13 == Dependency("jolokia","1.3.0",false,false))
    assert(Resolution.satisfies(jolokia13vr, feature("jolokia", "1.3.0")))
    assert(!Resolution.satisfies(jolokia13vr, feature("jolokia", "1.2.0")))
    assert(!Resolution.satisfies(jolokia13vr, feature("jolokia", "1.4.0")))
  }

  def `finds all transitive features and bundles`(): Unit = {
    val complete = repoIDs.keys.flatMap(getRepo).toSet
    val jolokiaDeps = Resolution.selectFeatureDeps(jolokia13, complete.flatMap(_.features))
    assert(Set(Dependency("http",None,false,false)) == jolokiaDeps)
    val result = Resolution.resolveRequiredFeatures(Set(jolokia13), complete)
    assert(result.isRight)
    val Right(rs) = result
    assert(Set("jolokia", "http", "pax-http", "pax-http-jetty", "pax-jetty") == rs.map(_.name))
    rs.flatMap(_.deps).collect { case Bundle(MavenUrl(url), _, _, _) => url }.foreach(println)
  }

  def `can write a valid features descriptor`(): Unit = {
    val repository = Repository("mvn:org.ops4j.pax.web/pax-web-features/4.1.4/xml/features")
    val bundle = Bundle("mvn:org.scala-lang/scala-library/2.11.7")
    val config = Config("my.project.cfg", "property=value")
    val feature = Feature(name = "test-feature", deps = Set(bundle, config))
    val descriptor = FeaturesXml("test-project", Seq(repository, feature))
    val xml = FeaturesXmlFormats.makeFeaturesXml(descriptor)
    println(xml)
    IO.withTemporaryFile("descriptor", new File("./target/test-data").getName) { f =>
      ThisUtil.write(f, FeaturesXmlFormats.featuresXsd, xml)
    }
  }

}