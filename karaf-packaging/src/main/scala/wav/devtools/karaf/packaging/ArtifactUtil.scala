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
    else Try(Util.validateXml(file.getCanonicalPath, getClass.getResourceAsStream("/" + FeaturesXmlFormats.featuresXsd))) match {
      case Failure(ex) => println(ex); false
      case Success(_) => true
    }

  def readFeaturesXml(file: File): Option[FeaturesXml] =
    if (!isValidFeaturesXml(file)) None
    else FeaturesXmlFormats.readFeaturesXml(XML.loadFile(file))

}
