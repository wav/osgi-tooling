package wav.devtools.util

import collection.JavaConversions._
import java.io.InputStream
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.{ Validator => JValidator }
import org.xml.sax.SAXException
import java.io.FileWriter
import javax.xml.transform.stream.StreamResult
import java.io.StringWriter

object Util {

  def injectProperties(inFile: String, properties: Map[String, String]): String = {
    import org.apache.commons.lang3.text.StrSubstitutor
    val sub = new StrSubstitutor(properties)
    sub.replace(io.Source.fromFile(inFile).mkString)
  }

  def formatXml(inFile: String): String = {
    val transformer = TransformerFactory.newInstance.newTransformer
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    val result = new StreamResult(new StringWriter())
    transformer.transform(new StreamSource(inFile), result)
    result.getWriter.toString
  }

  def validateXml(xmlFile: String, xsdFile: InputStream) {
    val schemaLang = "http://www.w3.org/2001/XMLSchema"
    val factory = SchemaFactory.newInstance(schemaLang)
    val schema = factory.newSchema(new StreamSource(xsdFile))
    val validator = schema.newValidator()
    validator.validate(new StreamSource(xmlFile))
  }

  def readTextFromJar(jar: File, path: String): String = {
    import java.io._
    import java.util.jar._
    import java.util.zip._
    val jarFile = new JarFile(jar.getCanonicalPath)
    val entry = jarFile.getEntry(path)
    val inStream = new BufferedInputStream(jarFile.getInputStream(entry))
    var s = ""
    var bytesRead = 0
    val buffer = new Array[Byte](2048)
    do {
      s += new String(buffer, 0, bytesRead)
      bytesRead = inStream.read(buffer)
    } while (bytesRead != -1)
    s
  }

}