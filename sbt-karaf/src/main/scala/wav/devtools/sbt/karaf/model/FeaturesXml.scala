package wav.devtools.sbt.karaf.model

import java.io.File

import scala.collection.mutable
import scala.xml.{Elem, XML}
import wav.devtools.sbt.karaf.util.Xml

object FeaturesXml {

   abstract class FeatureElem {
     private [FeaturesXml] def xml: Elem
   }

   case class Bundle(url: String) extends FeatureElem {
     private [model] lazy val xml =
       <bundle>{url}</bundle>
   }

   case class Feature(name: String, version: Option[String] = None, elems: Set[FeatureElem] = Set.empty) {
     private lazy val attrs: Map[String,String] = {
       val m = mutable.Map[String, String]("name" -> name)
       version.foreach(v => m.update("version", v))
       m.toMap
     }
     private [model] lazy val xml =
       Xml.setAttrs(<feature>{ elems.map(_.xml) }</feature>, attrs)
     lazy val Ref = FeatureRef(name, version)
   }

   case class FeatureRef(name: String, version: Option[String] = None) extends FeatureElem {
     private lazy val attrs: Map[String,String] = version.map(v => Map("version" -> v)).getOrElse(Map())
     private [model] lazy val xml =
       Xml.setAttrs(<feature>{ name }</feature>, attrs)
   }

   val XMLNS  = "http://karaf.apache.org/xmlns/features/v1.2.0"
   val XSD = "org/apache/karaf/features/karaf-features-1.2.0.xsd"
   def toXml(name: String, features: Seq[Feature], target: Option[File] = None): Elem =
     <features xmlns={ XMLNS } name={ name }>{
       target.filter(_.exists).map { target =>
         XML.loadFile(target) \\ "features" match {
           case Elem(prefix, label, attribs, scope, _, existingElements @ _*) =>
             Elem(prefix, label, attribs, scope, true, existingElements ++ features.map(_.xml): _*)
         }
       }.getOrElse(features.map(_.xml))
     }</features>

 }
