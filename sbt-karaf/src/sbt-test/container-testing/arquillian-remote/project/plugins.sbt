libraryDependencies += "wav.devtools" %% "karaf-manager" % sys.props("project.version")

addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % sys.props("project.version"))

addSbtPlugin("wav.devtools" % "sbt-karaf" % sys.props("project.version"))