## What's this?

Some tooling for OSGi in Scala.

## Status

This is an experiment, things are very likely to change. Further work on this is not planned.

Tested with Karaf 4.0.2 on OS X. Should work on windows.

|         | Status   |
|---------|:--------:|
| Unix    | [![Travis](https://travis-ci.org/wav/osgi-tooling.svg?branch=master)](https://travis-ci.org/wav/osgi-tooling) |
| Windows | [AppVeyor](https://ci.appveyor.com/project/wav/osgi-tooling/history) |

## [Karaf](https://karaf.apache.org/) plugins for SBT

## Getting started

### [Karaf Packaging Plugin Documentation](KarafPackagingPlugin.md)

plugins.sbt
```scala
lazy val plugins = (project in file("."))
  .dependsOn(sbtKarafPackaging)

def sbtKarafPackaging = ProjectRef(
	uri("git://github.com/wav/osgi-tooling.git#e390f0b948586e2ea3e6d18b2b44dd5be3669508"),
	"sbt-karaf-packaging")
```

### [Karaf Plugin Documentation](KarafPlugin.md)

plugins.sbt
```scala
lazy val plugins = (project in file("."))
  .dependsOn(sbtKaraf)

def sbtKaraf = ProjectRef(
	uri("git://github.com/wav/osgi-tooling.git#e390f0b948586e2ea3e6d18b2b44dd5be3669508"),
	"sbt-karaf")
```

### Troubleshooting

When using an updated version of the plugin, you may need to clear the cached plugin. You can see how this done in `./clean.sh`. Failure to do so may cause your IDE to hang or dependencies that the plugin depends on may not be resolved. 
