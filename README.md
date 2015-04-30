## What's this?

Some tooling for OSGi in Scala.

## Status

This is an experiment, things are very likely to change. Further work on this is not planned.

## [Karaf](https://karaf.apache.org/) plugins for SBT

Tested with Karaf 4.0.0.M2, PaxExam 4.5.0 on OS X.

## Getting started

Clone this repository and run `sbt publish-local`. Afterwards you'll be able to reference the contained plugins.

## sbt-karaf-packaging

Provides `SbtPackagingKaraf.featuresSettings` (by default) which is for creating a features.xml artifact

View the available settings in [SbtPackagingKaraf.scala](sbt-karafpackaging/src/main/scala/wav/devtools/sbt/karaf/packaging/SbtKarafPackaging.scala).

#### Usage

plugins.sbt
```scala
externalResolvers := Seq(ivyLocal)

addSbtPlugin(org % "sbt-karaf-packaging" % version)
```

build.sbt
```scala
import wav.devtools.sbt.karaf.packaging.SbtKarafPackaging

enablePlugins(SbtKarafPackaging)
```

*Run the generate features.xml task*

```bash
sbt> featuresXml
```

#### TODO

- Generate a different feature file based on configurations so that bundle URL's aren't pointing to the local file system. 
        Eg. do `<bundle>file://...</bundle>` and `<bundle>mvn:...</bundle>`
- Create a KAR

## sbt-karaf

Extends `sbt-karaf-packaging` with the additional functionality:
 
 - `SbtKaraf.paxSettings` is for running a pax test with the pax version defined in this plugin. \[SLOW]
 - `SbtKaraf.karafSettings` is for refreshing bundles and features by managing a karaf container sorta like `sbt-revolver`. \[FAST]
     + An alternative to dumping bundles in the `/deploy` folder.
 
Defaults are `SbtKaraf.karafSettings +++ SbtPackagingKaraf.featuresSettings`

View the available settings in [SbtKaraf.scala](sbt-karaf/src/main/scala/wav/devtools/sbt/karaf/SbtKaraf.scala).

#### Usage

plugins.sbt
```scala
externalResolvers := Seq(ivyLocal)

addSbtPlugin(org % "sbt-karaf" % version)
```

build.sbt
```scala
import wav.devtools.sbt.karaf.SbtKaraf

enablePlugins(SbtKaraf)

// some sbt-osgi settings ...

```

##### Running the tasks

Karaf must be running and accepting connections to it's RMI registry. The endpoint to the RMI registry is configured with Karaf's defaults in the setting `KarafKeys.karafContainerArgs`.

```bash
sbt> show karafContainerArgs // shows karaf defaults if not changed.
sbt> karafRefreshBundle
sbt> undeployProjectFeature
```

Problems with feature/bundle deployments that aren't visible in SBT can be viewed inside Karaf. Eg.

```bash
karaf> log:tail
```

## Moar! *please*

See [examples](examples) and [notes](sbt-karaf/src/sbt-test.project/README.md) for usage. All examples reference [props.scala](project/props.scala) and are `sbt-scripted` tests.
