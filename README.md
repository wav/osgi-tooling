## What's this?

Some tooling for OSGi in Scala.

## Status

This is an experiment, things are very likely to change. Further work on this is not planned.

Tested with Karaf 4.0.0 on OS X. Should work on windows.

## [Karaf](https://karaf.apache.org/) plugins for SBT

## Getting started

Clone this repository and run `sbt publish-local`. Afterwards you'll be able to reference the contained plugins.

### [Karaf Packaging Plugin Documentation](KarafPackagingPlugin.md)

plugins.sbt
```scala
addSbtPlugin("wav.devtools" % "sbt-karaf-packaging" % version)
```

### [Karaf Plugin Documentation](KarafPlugin.md)

plugins.sbt
```scala
addSbtPlugin("wav.devtools" % "sbt-karaf" % version)
```
