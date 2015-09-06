## sbt-karaf

Extends the [Karaf Packaging Plugin](KarafPackagingPlugin.md) with settings for refreshing bundles and features by managing a karaf container. It provides quick feedback during development. It's alternative to using the karaf `/deploy` folder.

View the available settings in:
  >[Keys.scala](sbt-karaf/src/main/scala/wav/devtools/sbt/karaf/Keys.scala)
  >[Defaults.scala](sbt-karaf/src/main/scala/wav/devtools/sbt/karaf/Defaults.scala)

See the examples, [here](sbt-karaf/src/sbt-test).

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

### Other settings

> `paxSettings` is for running a pax test with the pax version defined in this plugin. Treat it as informational, it's slow.
