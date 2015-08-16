## What's this?

Some tooling for OSGi in Scala.

## Status

This is an experiment, things are very likely to change. Further work on this is not planned.

## [Karaf](https://karaf.apache.org/) plugins for SBT

Tested with Karaf 4.0.0, PaxExam 4.5.0 on OS X.

## Getting started

Clone this repository and run `sbt publish-local`. Afterwards you'll be able to reference the contained plugins.

### [Karaf Packaging Plugin Documentation](KarafPackagingPlugin.md)

plugins.sbt
```scala
externalResolvers := Seq(ivyLocal)

addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % version)
```

### [Karaf Plugin Documentation](KarafPlugin.md)

plugins.sbt
```scala
externalResolvers := Seq(ivyLocal)

addSbtPlugin("wav.devtools" % "sbt-karaf" % version)
```

## Moar! *please*

See [examples](examples) and [notes](sbt-karaf/src/sbt-test.project/README.md) for usage.

All examples reference [props.scala](project/props.scala) and are `sbt-scripted` tests.
