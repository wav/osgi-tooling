package wav.devtools.sbt.karaf.packaging.model

import sbt.ModuleID

sealed trait FeaturesXmlModuleID {
  val module: ModuleID
}

case class FeaturesModuleID(module: ModuleID, features: Set[FeaturesXml.FeatureRef])
  extends FeaturesXmlModuleID

case class WrappedBundleModuleID(module: ModuleID, manifest: Seq[(sbt.ArtifactFilter, Map[String, String])])
  extends FeaturesXmlModuleID

trait ModuleSyntax {

  import FeaturesXml._

  implicit class RichFeaturesModuleID(m: ModuleID) {
    def features(features: String*): FeaturesXmlModuleID =
      FeaturesModuleID(m classifier ("features"), features.map(fname => fname.split("/") match {
        case Array(name) => FeatureRef(name, None)
        case Array(name, version) => FeatureRef(name, Some(version))
        case _ => assert(false, s"Invalid feature name '$fname' defined for '$m'"); emptyFeatureRef
      }).toSet)
  }

  implicit class RichWrappedBundleModuleID(m: ModuleID) {
    def wrapBundle(manifests: (sbt.ArtifactFilter, Map[String, String])*): FeaturesXmlModuleID =
      WrappedBundleModuleID(m: ModuleID, manifests)
  }

}