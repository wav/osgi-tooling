val V = System.getProperty("project.version", "NOT_SET")

libraryDependencies += "wav.devtools" %% "karaf-packaging" % V

addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % V)