val ivyLocal = Resolver.file("local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

externalResolvers := Seq(ivyLocal)

addSbtPlugin("wav.devtools" % "sbt-karaf" % sys.props("project.version"))

lazy val plugins = (project in file("."))
  .dependsOn(sbtOsgi)

def sbtOsgi = uri("git://github.com/sbt/sbt-osgi.git")

updateOptions := updateOptions.value.withCachedResolution(true)