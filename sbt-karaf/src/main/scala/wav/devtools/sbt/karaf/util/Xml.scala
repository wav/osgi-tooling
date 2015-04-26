package wav.devtools.sbt.karaf.util

import collection.mutable
import java.io.File
import sbt.IO

import scala.xml._

object Xml {

  def write(target: File, source: File, xsd: String, props: Map[String,String], elems: Elem): File = {
    import wav.devtools.util.Util._
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


