val V = System.getProperty("project.version", "NOT_SET")

libraryDependencies ++= Seq(
  "wav.devtools" %% "karaf-packaging" % V,
  "wav.devtools" %% "karaf-manager" % V)

addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % V)

addSbtPlugin("wav.devtools" % "sbt-karaf" % V)