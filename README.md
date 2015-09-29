## What's this?

Some tooling for OSGi in Scala.

## Status

This is an experiment, things are very likely to change. Further work on this is not planned.

Tested with Karaf 4.0.1 on OS X. Should work on windows.

[![Build Status](https://travis-ci.org/wav/osgi-tooling.svg?branch=master)](https://travis-ci.org/wav/osgi-tooling)

## [Karaf](https://karaf.apache.org/) plugins for SBT

## Getting started

### [Karaf Packaging Plugin Documentation](KarafPackagingPlugin.md)

plugins.sbt
```scala
lazy val plugins = (project in file("."))
  .dependsOn(sbtKarafPackaging)

def sbtKarafPackaging = ProjectRef(
	uri("git://github.com/wav/osgi-tooling.git#888065ecab60781d8d8ae4b15e9199f17d24908f"),
	"sbt-karaf-packaging")
```

### [Karaf Plugin Documentation](KarafPlugin.md)

plugins.sbt
```scala
lazy val plugins = (project in file("."))
  .dependsOn(sbtKaraf)

def sbtKaraf = ProjectRef(
	uri("git://github.com/wav/osgi-tooling.git#888065ecab60781d8d8ae4b15e9199f17d24908f"),
	"sbt-karaf")
```
