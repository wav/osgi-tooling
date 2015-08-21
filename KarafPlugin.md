## sbt-karaf

Extends the [Karaf Packaging Plugin](KarafPackagingPlugin.md) with the additional functionality:

 - `paxSettings` is for running a pax test with the pax version defined in this plugin. \[SLOW]
 - `karafSettings` is for refreshing bundles and features by managing a karaf container sorta like `sbt-revolver`. \[FAST]
     + An alternative to dumping bundles in the `/deploy` folder.

View the available settings in:
  >[Keys.scala](sbt-karaf-packaging/src/main/scala/wav/devtools/sbt/karaf/Keys.scala)
  >[Defaults.scala](sbt-karaf-packaging/src/main/scala/wav/devtools/sbt/karaf/Defaults.scala)

#### Quick start

build.sbt
```scala
import wav.devtools.sbt.karaf.SbtKaraf
import SbtKaraf.autoImport._
import KarafKeys._
import KarafPackagingKeys._

enablePlugins(SbtKaraf)

// add some sbt-osgi settings ...

```

##### Running the tasks

Karaf must be running and accepting connections to it's RMI registry. The endpoint to the RMI registry is configured with Karaf's defaults in the setting `karafContainerArgs`.

```bash
sbt> show karafContainerArgs # shows karaf defaults if not changed.
sbt> karafRefreshBundle
sbt> undeployProjectFeature
```

Problems with feature/bundle deployments that aren't visible in SBT can be viewed inside Karaf. Eg.

```bash
karaf> log:tail
```
