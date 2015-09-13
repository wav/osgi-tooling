package wav.devtools.sbt.karaf.packaging.model

import java.net.URI

// sbt-maven-resolver doesn't like artifacts that are non jar that don't have a classifier. (sbt 0.13.9)
// So we use URI instead of ModuleID and download things manually.
case class KarafDistribution(uri: URI, contentPath: String = null)