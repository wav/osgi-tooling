## Before running a Pax test, you can cache the karaf bundle in the local .m2. If it's not cached the test will timeout.

    `mvn dependency:get -Dartifact=org.apache.karaf:apache-karaf:4.0.0.M2:tar.gz -Dtransitive=false`