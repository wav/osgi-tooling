package wav.devtools.sbt.karaf.packaging

import java.io.{File, FileInputStream, InputStream, StringWriter}
import java.net.{URI, URL}
import java.util.jar.{JarInputStream, Manifest => JManifest}
import javax.xml.transform.stream.{StreamResult, StreamSource}
import javax.xml.transform.{OutputKeys, TransformerFactory}
import javax.xml.validation.SchemaFactory

import org.apache.commons.lang3.text.StrSubstitutor
import org.rauschig.jarchivelib._
import sbt.{IO, MavenRepository}

import scala.collection.JavaConversions._
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

  def write(target: File, xsd: String, elems: Elem, source: Option[(File, Map[String, String])] = None): File = {
    if (target.exists) target.delete
    source.foreach { t =>
      val (f, props) = t
      if (f.exists) IO.write(target, injectProperties(f.getCanonicalPath, props))
    }
    if (!target.getParentFile.exists) target.getParentFile.mkdirs
    XML.save(target.getCanonicalPath, elems, "UTF-8", true, null)
    val formatted = formatXml(target.getCanonicalPath)
    IO.write(target, formatted)
    validateXml(target.getCanonicalPath, this.getClass.getClassLoader.getResourceAsStream(xsd))
    target
  }

  def setAttrs(e: Elem, attrs: Map[String, Option[String]]): Elem =
    e.copy(attributes = attrs.collect {
      case ((name, Some(value))) => Attribute(None, name, Text(value.toString), Null)
    }.fold(Null)((soFar, attr) => soFar append attr))

  def getJarManifest(path: String): JManifest = {
    val is = new FileInputStream(path)
    val jar = new JarInputStream(is)
    jar.getManifest
  }

  def unpack(archive: File, outDir: File, overwrite: Boolean = false): Unit = {
    require(archive.exists(), s"$archive not found.")
    if (overwrite && outDir.exists()) outDir.delete()
    if (!outDir.exists()) {
      val archiver: Archiver =
        if (archive.getName.endsWith(".tar.gz")) {
          ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        } else if (archive.getName.endsWith(".zip")) {
          ArchiverFactory.createArchiver(ArchiveFormat.ZIP)
        } else {
          sys.error(s"Unknown format for $archive")
          ???
        }
      archiver.extract(archive, outDir)
    }
  }

  def download(uri: URI, collect: File => File, resolvers: Seq[MavenRepository] = Seq.empty): Option[File] = {
    def tryDownload(url: URL): Option[File] =
      IO.withTemporaryFile("download", "") { f =>
        var downloaded = false
        try {
          val proto = url.getProtocol()
          if (proto.startsWith("http")) {
            IO.download(url, f)
            downloaded = true
          } else if (proto == null || proto == "file") {
            downloaded = new File(uri).exists()
          }
        } catch {
          case _: Exception =>
            println(s"Failed to download ${uri.toString}")
        }
        if (downloaded) Some(collect(f)) else None
      }
    val result: Option[File] = uri.getScheme match {
      case "file" =>
        Some(new File(uri))
      case "mvn" =>
        val path = org.apache.karaf.util.maven.Parser.pathFromMaven(uri.toString)
        // true > false, so .sortBy(!_.isCache) will select cache repos first.
        resolvers.sortBy(!_.isCache).flatMap { r =>
          val url = new URL(s"${r.root}$path")
          tryDownload(url)
        }.headOption
      case "http" | "https" =>
        tryDownload(uri.toURL)
      case _: String => None
    }
    result.filter(_.exists())
  }

}


