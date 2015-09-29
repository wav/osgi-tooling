#!/bin/bash

if [[ ! -d .git ]]; then
  echo "This must be run at the base of the git repository" >&2
  exit 1
fi

./build/get-sbt.sh

# run sbt

# `build.commands` is list of sbt commands to be run
commands=\;`cat build.commands | sed '/^-/d' | tr '\n' ';'`
commands=${commands%;}

. ./docker/util.sh

mvnGet org.apache.karaf:apache-karaf-minimal:4.0.1:tar.gz karaf.tar.gz || exit 1

tarFromOutDir karaf.tar.gz karaf || exit 1

echo sbt $commands

./bin/sbt "$commands"