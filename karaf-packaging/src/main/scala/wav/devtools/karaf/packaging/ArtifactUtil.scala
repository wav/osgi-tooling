package wav.devtools.karaf.packaging

import java.io.File

import scala.util.{Success, Failure, Try}
import scala.xml.XML

private [devtools] object ArtifactUtil {

  def getSymbolicName(file: File): Option[String] =
    Option(
      Util.getJarManifest(file)
        .getMainAttributes
        .getValue("Bundle-SymbolicName")
    ).filterNot(_.isEmpty)

  def isValidFeaturesXml(file: File): Boolean =
    if (!file.exists()) false
    else {
      val xsds = FeaturesXmlFormats.featuresSchemas.map(_._2._2)
      var result: Try[Unit] = Failure(new IllegalArgumentException("xsds must be non empty"))
      for (i <- 0 to xsds.size) {
        result = Try(Util.validateXml(file.getCanonicalPath, getClass.getResourceAsStream("/" + xsds(i))))
        if (result.isSuccess) return true
      }
      val Failure(ex) = result
      println(ex)
      false
    }

  def readFeaturesXml(file: File): Option[FeaturesXml] =
    if (!isValidFeaturesXml(file)) None
    else FeaturesXmlFormats.readFeaturesXml(XML.loadFile(file))

}
