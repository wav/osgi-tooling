#!/bin/bash

rm -Rf ~/.ivy2/local/wav.devtools/sbt-karaf ~/.ivy2/local/wav.devtools/sbt-karaf-packaging
find . \( -iname ".DS_Store" -o -name "target" -o -name "project\target" \) -exec rm -Rf {} \;