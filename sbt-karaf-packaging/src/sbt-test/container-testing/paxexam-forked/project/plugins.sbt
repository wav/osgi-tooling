val V = System.getProperty("project.version", "NOT_SET")

libraryDependencies += "wav.devtools" %% "karaf-packaging" % V

addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % V)

lazy val plugins = (project in file("."))
  .dependsOn(sbtOsgi)

def sbtOsgi = uri("git://github.com/sbt/sbt-osgi.git")