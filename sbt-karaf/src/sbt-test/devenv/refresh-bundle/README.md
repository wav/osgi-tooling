## Usage

Inspect `./sbtDebug` to see how to start the sbt prompt.

This project serves as a test for the following workflow:

- Starting a managed karaf server. `karafStartServer`
> HINT: You can login to the instance that is managed by sbt in the folder `target/karaf-dist/KARAF_DIST/bin/client`
- Deploying a bundle to a karaf container. `karafUpdateBundle`
- Refresh that bundle when changes are made. `~ karafUpdateBundle`
- Undeploy the feature. `karafUndeployFeature`
- Reset the karaf server when everything is falling to pieces. `karafResetServer` 

## What's included

1 bundle with 2 "services", each print messages to the log, they are:

- A Printer service that starts with the Bundle Activator and prints json objects.
- A blueprint service that prints messages on an interval defined by a camel timer.

## User Experience

When using the `karafUpdateBundle` task, you should see the **karaf shell** update like so:

```bash
karaf@root()> bundle:list
START LEVEL 100 , List Threshold: 50
ID | State  | Lvl | Version                            | Name
----------------------------------------------------------------------------------------------------
35 | Active |  80 | 0.1.0.SNAPSHOT                     | refresh-bundle
36 | Active |  80 | 2.11.7.v20150622-112736-1fbce4612c | Scala Standard Library
37 | Active |  80 | 0                                  | wrap_..._json_20140107_json-20140107.jar
```


```bash
karaf@root()> bundle:restart 35
Exception in thread "Thread-58" java.lang.InterruptedException: sleep interrupted
    at java.lang.Thread.sleep(Native Method)
    at wav.devtools.sbt.karaf.examples.refreshbundle.impl.PrinterService.resume(PrinterService.scala:27)
    at wav.devtools.sbt.karaf.examples.refreshbundle.Activator$$anon$1.run(Activator.scala:24)
Unregistered wav.devtools.sbt.karaf.examples.refreshbundle.PrinterService
Registering wav.devtools.sbt.karaf.examples.refreshbundle.PrinterService
Registered wav.devtools.sbt.karaf.examples.refreshbundle.PrinterService
Exception in thread "Thread-56" Unregistered wav.devtools.sbt.karaf.examples.refreshbundle.PrinterServicejava.lang.InterruptedException: sleep interrupted

    at java.lang.Thread.sleep(Native Method)
    at wav.devtools.sbt.karaf.examples.refreshbundle.impl.PrinterService.resume(PrinterService.scala:27)
    at wav.devtools.sbt.karaf.examples.refreshbundle.Activator$$anon$1.run(Activator.scala:24)
Registering wav.devtools.sbt.karaf.examples.refreshbundle.PrinterService
Registered wav.devtools.sbt.karaf.examples.refreshbundle.PrinterService
```

In the **karaf logging console** you should see messages like this when it's active:

```bash
2015-09-24 23:54:11,519 | INFO | Thread-11 | PrinterService | 35 - default.refresh.bundle - 0.1.0.SNAPSHOT | {"controller event":"resumed"}
2015-09-24 23:54:11,519 | INFO | Thread-11 | PrinterService | 35 - default.refresh.bundle - 0.1.0.SNAPSHOT | {"controller notification":"alive"}
2015-09-24 23:54:11,557 | INFO | Thread-13 | PrinterService | 35 - default.refresh.bundle - 0.1.0.SNAPSHOT | {"controller event":"resumed"}
2015-09-24 23:54:11,557 | INFO | Thread-13 | PrinterService | 35 - default.refresh.bundle - 0.1.0.SNAPSHOT | {"controller notification":"alive"}
2015-10-19 20:14:04,830 | INFO | ...       | Blueprint...   | 48 - org.apache.camel.camel-core - 2.16.0 | Apache Camel 2.16.0 (CamelContext: camel-2) started in 0.103 seconds
2015-10-19 20:14:05,849 | INFO | timer://test | test        | 48 - org.apache.camel.camel-core - 2.16.0 | Exchange[ExchangePattern: InOnly, BodyType: null, Body: [Body is null]]
```
