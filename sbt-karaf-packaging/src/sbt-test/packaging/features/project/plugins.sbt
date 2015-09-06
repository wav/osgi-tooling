addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % sys.props("project.version"))

updateOptions := updateOptions.value.withCachedResolution(true)