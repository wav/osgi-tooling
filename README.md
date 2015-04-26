## What's this?

Some tooling for osgi in scala.

## Status

An experiment. Further work on this is not planned.

## sbt-[karaf](https://karaf.apache.org/) plugin

Tested with Karaf 4.0.0.M2, PaxExam 4.5.0 on OS X.

 There are 3 sets of functionality provided by this plugin
 - `SbtKaraf.featuresSettings` is for creating a features.xml artifact (default)
 - `SbtKaraf.paxSettings` is for running a pax test with the pax version defined in this plugin
 - `SbtKaraf.karafSettings` is for refreshing bundles and features by managing a karaf instance (via. RMI on a local jvm), sorta like `sbt-revolver`. \[INCOMPLETE]

#### Usage

See [examples](examples) for usage. All examples reference [props.scala](project/props.scala) and are `sbt-scripted` tests.

Clone this repository and run `sbt publish-local`. Afterwards you'll be able to reference this plugin using.