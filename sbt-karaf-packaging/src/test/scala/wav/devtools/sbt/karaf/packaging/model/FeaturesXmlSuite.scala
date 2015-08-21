package wav.devtools.sbt.karaf.packaging.model

import org.scalatest.Spec
import wav.devtools.sbt.karaf.packaging.Resolution
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._

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

  val jolokia = featureRef("jolokia","1.3.0")

  def `should read valid feature files`(): Unit = {
    val Some(fr) = getRepo(standardID)
    val paxRepo = Repository("mvn:org.ops4j.pax.web/pax-web-features/4.1.4/xml/features")
    assert(fr.featuresXml.elems.contains(paxRepo))
    val jolokia = feature("jolokia","1.3.0",Set(Bundle("mvn:org.jolokia/jolokia-osgi/1.3.0"), FeatureRef("http",None)))
    assert(fr.featuresXml.elems.contains(jolokia))
  }

  def `should identify feature that is defined but not referenced`(): Unit = {
    val Some(fr) = getRepo(standardID)
    fr.features.exists(Resolution.satisfies(jolokia, _))
  }

  def `should identify a feature dependency that is not defined but referenced`(): Unit = {
    val Some(fr) = getRepo(standardID)
    fr.features.exists(Resolution.satisfies(FeatureRef("pax-http", None), _))
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
    assert(jolokia == featureRef("jolokia","1.3.0"))
    assert(Resolution.satisfies(featureRef("jolokia","[1.3,1.4)"), feature("jolokia", "1.3.0")))
    assert(!Resolution.satisfies(featureRef("jolokia","[1.3,1.4)"), feature("jolokia", "1.2.0")))
    assert(!Resolution.satisfies(featureRef("jolokia","[1.3,1.4)"), feature("jolokia", "1.4.0")))
  }

  def `finds all transitive features and bundles`(): Unit = {
    val complete = repoIDs.keys.flatMap(getRepo).toSet
    val jolokiaDeps = Resolution.selectFeatureDeps(jolokia, complete.flatMap(_.features))
    assert(Set(FeatureRef("http")) == jolokiaDeps)
    val result = Resolution.resolveRequiredFeatures(Set(jolokia), complete)
    assert(result.isRight)
    val Right(rs) = result
    assert(Set("jolokia", "http", "pax-http", "pax-http-jetty", "pax-jetty") == rs.map(_.name))
    rs.flatMap(_.bundles).collect { case Bundle(MavenUrl(url)) => url }.foreach(println)
  }

}