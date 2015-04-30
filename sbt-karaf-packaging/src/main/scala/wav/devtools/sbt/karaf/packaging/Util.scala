package wav.devtools.sbt.karaf.packaging

import java.io.{File, InputStream, StringWriter}
import javax.xml.transform.{OutputKeys, TransformerFactory}
import javax.xml.transform.stream.{StreamResult, StreamSource}
import javax.xml.validation.SchemaFactory
import collection.JavaConversions._
import org.apache.commons.lang3.text.StrSubstitutor
import sbt.IO

import scala.xml._

private[packaging] object Util {

  def injectProperties(filePath: String, properties: Map[String, String]): String =
    StrSubstitutor.replace(io.Source.fromFile(filePath).mkString, properties)

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

  def write(target: File, source: File, xsd: String, props: Map[String, String], elems: Elem): File = {
    if (target.exists) target.delete
    if (source.exists) {
      IO.write(target, injectProperties(source.getCanonicalPath, props))
    }
    if (!target.getParentFile.exists) target.getParentFile.mkdirs
    XML.save(target.getCanonicalPath, elems, "UTF-8", true, null)
    val formatted = formatXml(target.getCanonicalPath)
    IO.write(target, formatted)
    validateXml(target.getCanonicalPath, this.getClass.getClassLoader.getResourceAsStream(xsd))
    target
  }

  def setAttrs(e: Elem, attrs: Map[String, String]): Elem =
    e.copy(attributes = attrs.map(t =>
      Attribute(None, t._1, Text(t._2), Null)).fold(Null)((soFar, attr) => soFar append attr))
}


