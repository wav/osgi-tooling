## sbt-karaf-packaging

Adds `SbtPackagingKaraf.featuresSettings` to the build which is for creating a features.xml artifact.

View the available settings in:
  >[Keys.scala](sbt-karaf-packaging/src/main/scala/wav/devtools/sbt/karaf/packaging/Keys.scala)
  >[Defaults.scala](sbt-karaf-packaging/src/main/scala/wav/devtools/sbt/karaf/packaging/Defaults.scala)

#### Quick start

build.sbt
```scala
import wav.devtools.sbt.karaf.packaging.{SbtKarafPackaging, FeaturesRepositoryID}
import SbtKarafPackaging.autoImport._

enablePlugins(SbtKarafPackaging)

featuresRequired := Map("jolokia" -> "1.3.0", "scr" -> "*") // version ranges and None not implemented.

libraryDependencies += "org.slf4j" % "osgi-over-slf4j" % "1.7.10"

libraryDependencies ++= Seq(
  FeaturesRepositoryID("org.apache.karaf.features", "standard", "4.0.0"),
  FeaturesRepositoryID("org.apache.karaf.features", "enterprise", "4.0.0"))
```

When you run the generate features.xml task:

```bash
sbt> generateFeaturesFile
[info] Some($HOME/Repositories/github/wav/osgi-tooling/sbt-karaf-packaging/src/sbt-test/packaging/features/target/scala-2.10/features.xml
```

A features file like the following will be generated:

`{baseDir}/target/scala-2.10/features.xml`

```
<?xml version="1.0" encoding="UTF-8"?><features xmlns="http://karaf.apache.org/xmlns/features/v1.3.0" name="features">
  <feature name="features" version="0.1.0.SNAPSHOT">
    <bundle>mvn:org.scala-lang/scala-library/2.10.5/jar</bundle>
    <bundle>file:/Users/wassim/Repositories/github/wav/osgi-tooling/sbt-karaf-packaging/src/sbt-test/packaging/features/target/scala-2.10/features_2.10-0.1.0.SNAPSHOT.jar</bundle>
    <feature version="1.3.0">jolokia</feature>
    <feature>scr</feature>
    <bundle>mvn:org.slf4j/osgi-over-slf4j-1.7.10/1.7.10/bundle</bundle>
  </feature>
</features>
```

### Modifying the features file

```
import wav.devtools.sbt.karaf.packaging.model.FeaturesXml._

lazy val projectA = project.in(file("A"))
    .enablePlugins(SbtKarafPackaging)

lazy val projectB = project.in(file("B"))
    .enablePlugins(SbtKarafPackaging)

lazy val root = project.in(file("."))
    .enablePlugins(SbtKarafPackaging)
    .dependsOn(projectA, projectB)
    .settings(
      featuresProjectFeature := {
        val pf = featuresProjectFeature.value
        pf.copy(deps = pf.deps :+ Configuration("property=value"))
      },
      featuresXml := {
        val xml = featuresXml.value
        xml.copy(elems = xml.elems :+
          (featuresProjectFeature in projectA).value :+
          (featuresProjectFeature in projectB).value) :+
          Repository("mvn:org.ops4j.pax.web/pax-web-features/4.1.4/xml/features")
      })
```

#### TODO

- Add bundles from non `mvn:` style urls to project feature
- Read and add configuration elements in a features file
- Report on bundles that cannot be added to the project feature with a suggestion
- Only add bundles that are not available in feature dependencies to the project feature
- Generate a different feature file based on configurations so that bundle URL's aren't pointing to the local file system.
        Eg. do `<bundle>file://...</bundle>` and `<bundle>mvn:...</bundle>`
        Work around: override the setting `featuresProjectBundle`
- Create a KAR