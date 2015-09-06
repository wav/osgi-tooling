## sbt-karaf-packaging

Creates a features.xml artifact.

View the available settings in:
  >[Keys.scala](sbt-karaf-packaging/src/main/scala/wav/devtools/sbt/karaf/packaging/Keys.scala)
  >[Defaults.scala](sbt-karaf-packaging/src/main/scala/wav/devtools/sbt/karaf/packaging/Defaults.scala)

See the examples, [here](sbt-karaf-packaging/src/sbt-test).

#### Quick start

build.sbt
```scala
import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging
import SbtKarafPackaging.autoImport._
import KarafPackagingKeys._

enablePlugins(SbtKarafPackaging)

featuresRequired := Map("jolokia" -> "1.3.0", "scr" -> "*")

libraryDependencies ++= Seq(
  "org.slf4j" % "osgi-over-slf4j" % "1.7.10",
  FeatureID("org.apache.karaf.features", "standard", "4.0.0"),
  FeatureID("org.ops4j.pax.web", "pax-web-features", "4.1.4"))
```

When you run the generate features.xml task:

```bash
> show featuresFile
[info] Some({baseDir}/target/scala-2.10/features.xml)
```

A features file like the following will be generated:

`{baseDir}/target/scala-2.10/features.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<features xmlns="http://karaf.apache.org/xmlns/features/v1.3.0" name="my-project">
  <repository>mvn:org.apache.karaf.features/standard/4.0.0/xml/features</repository>
  <feature version="0.1.0.SNAPSHOT" name="my-project">
    <feature>scr</feature>
    <bundle>mvn:org.scala-lang/scala-library/2.10.5/jar</bundle>
    <bundle>file:/{baseDir}/target/scala-2.10/my-project.10-0.1.0.SNAPSHOT.jar</bundle>
    <feature version="1.3.0">jolokia</feature>
    <bundle>mvn:org.slf4j/osgi-over-slf4j-1.7.10/1.7.10/bundle</bundle>
  </feature>
</features>
```

### Modifying the features file

```scala
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

### Other settings

> BYO features file
> `featuresFile := Some((resourceDirectory in Compile).value / "features.xml")`

> Add the dependencies of `featuresRequired` to `libraryDepencencies`. See settings for more info.
> `KarafPackagingKeys.featuresAddDependencies := true`

#### TODO

- Add bundles from non `mvn:` style urls to project feature
- Read and add configuration elements in a features file
- Report on bundles that cannot be added to the project feature with a suggestion
- Generate a different feature file based on configurations so that bundle URL's aren't pointing to the local file system.
        Eg. do `<bundle>file://...</bundle>` and `<bundle>mvn:...</bundle>`
        Work around: override the setting `featuresProjectBundle`
- Create a KAR