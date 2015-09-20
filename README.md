## What's this?

Some tooling for OSGi in Scala.

## Status

This is an experiment, things are very likely to change. Further work on this is not planned.

Tested with Karaf 4.0.1 on OS X. Should work on windows.

## [Karaf](https://karaf.apache.org/) plugins for SBT

## Getting started

### [Karaf Packaging Plugin Documentation](KarafPackagingPlugin.md)

plugins.sbt
```scala
lazy val plugins = (project in file("."))
  .dependsOn(sbtKarafPackaging)

def sbtKarafPackaging = ProjectRef(
	uri("git://github.com/wav/osgi-tooling.git#d029e0d273effaacdd50ab8daafe153a88763f0b"),
	"sbt-karaf-packaging")
```

### [Karaf Plugin Documentation](KarafPlugin.md)

plugins.sbt
```scala
lazy val plugins = (project in file("."))
  .dependsOn(sbtKaraf)

def sbtKaraf = ProjectRef(
	uri("git://github.com/wav/osgi-tooling.git#d029e0d273effaacdd50ab8daafe153a88763f0b"),
	"sbt-karaf")
```
