### Testing `sbt-karaf`

Start a karaf instance locally with defaults (see resources/arquillian.xml) and install a jmx bundle. E.g.

```shell
karaf@root()> bundle:install mvn:org.apache.aries.jmx/org.apache.aries.jmx/1.1.5
```

Afterwards you can run the `test` and `scripted` tests.