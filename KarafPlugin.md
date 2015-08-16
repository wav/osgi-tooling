## sbt-karaf

Extends the [Karaf Packaging Plugin](KarafPlugin.md) with the additional functionality:

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