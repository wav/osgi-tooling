#!/bin/bash

# download sbt

sbtVersion=`cat project/build.properties | grep sbt.version`
sbtVersion="${sbtVersion##*=}"
if [[ $sbtVersion = "" ]]; then
	echo Could read sbt.version >&2
	exit 1
fi
sbtUrl="http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$sbtVersion/sbt-launch.jar"

[[ ! -d bin ]] && mkdir bin

echo Downloading sbt $sbtUrl

curl --fail -L $sbtUrl.sha1 > bin/sbt-launch.jar.sha1
if [ $? -ne 0 ]; then
	echo Failed to download sbt >&2
	exit 1
fi

checkSbt() {
	local actualSHA1=`shasum bin/sbt-launch.jar`
	actualSHA1=${actualSHA1:0:40}
	[[ "$actualSHA1" == `cat bin/sbt-launch.jar.sha1` ]]
}

checkSbt || curl --fail -L $sbtUrl > bin/sbt-launch.jar
checkSbt
if [ $? -ne 0 ]; then
	echo Failed to download sbt >&2
	exit 1
fi

cat <<SBT_LAUNCH>bin/sbt
SBT_OPTS="-XX:+CMSClassUnloadingEnabled"
java $SBT_OPTS -jar ./bin/sbt-launch.jar "\$@"
SBT_LAUNCH

chmod +x bin/sbt