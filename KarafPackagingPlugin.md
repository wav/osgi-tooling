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

featuresRequired := Map(
	"wrap" -> "*" /* enable provisioning of wrapped bundles */, 
	"log" -> "*" /* implements slf4j */)

libraryDependencies ++= Seq(
	"org.json" % "json" % "20140107" toWrappedBundle(Map(
		"Bundle-SymbolicName" -> "json",
		"Bundle-Version" -> "20140107"
	)),
	"org.slf4j" % "slf4j-api" % "1.7.12" % "provided",
	"org.osgi" % "org.osgi.core" % "6.0.0" % "provided",
	FeatureID("org.apache.karaf.features", "standard", "4.0.2"))
```

When you run the generate features.xml task:

```bash
> show featuresFile
[info] Some({baseDir}/target/scala-2.10/features.xml)
```

A features file like the following will be generated:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<features xmlns="http://karaf.apache.org/xmlns/features/v1.3.0" name="my-project">
  <repository>mvn:org.apache.karaf.features/standard/4.0.2/xml/features</repository>
  <feature version="0.1.0.SNAPSHOT" name="my-project">
    <feature dependency="true" prerequisite="true">log</feature>
    <bundle>mvn:org.scala-lang/scala-library/2.10.5/jar</bundle>
    <bundle>wrap:mvn:org.json/json/20140107$Bundle-SymbolicName=json&amp;Bundle-Version=20140107</bundle>
    <bundle>file:/{baseDir}/target/scala-2.10/my-project.10-0.1.0.SNAPSHOT.jar</bundle>
    <feature dependency="true" prerequisite="true">wrap</feature>
  </feature>
</features>
```

### How it works.

1. The plugin will download all feature repositories/descriptors defined in `libraryDependencies`.
2. It will then collect all bundles defined by the `featuresRequired` and put those bundles in the provided scope if
   the setting `featuresAddDependencies := true`
3. All bundles in the compile scope and feature dependencies defined by `featuresRequired` will be added to a
   "project feature" in the `featuresProjectFeature` setting.

### Modifying the features file

```scala
import FeaturesXml._

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
        pf.copy(deps = pf.deps :+ Config("my.project.cfg", s"""
          | property1=value1
          | property2=value2
          |""".stripMargin))
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

#### TODO

- Report on bundles that cannot be added to the project feature with a suggestion
- Generate a different feature file based on configurations so that bundle URL's aren't pointing to the local file system.
        Eg. do `<bundle>file://...</bundle>` and `<bundle>mvn:...</bundle>`
        Work around: override the setting `featuresProjectBundle`