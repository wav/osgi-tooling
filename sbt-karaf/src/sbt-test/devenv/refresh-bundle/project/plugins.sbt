val V = System.getProperty("project.version", "NOT_SET")

libraryDependencies ++= Seq(
  "wav.devtools" %% "karaf-packaging" % V,
  "wav.devtools" %% "karaf-manager" % V)

addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % V)

addSbtPlugin("wav.devtools" % "sbt-karaf" % V)

//lazy val plugins = (project in file("."))
//  .dependsOn(sbtOsgi)
//
//def sbtOsgi = uri("git://github.com/sbt/sbt-osgi.git")

addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.8.0")