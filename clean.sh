#!/bin/bash

rm -Rf ~/.ivy2/local/wav.devtools/{sbt-karaf,sbt-karaf-packaging} \ 
	~/.sbt/0.13/{dependency,staging}
find . \( -iname ".DS_Store" -o -name "target" -o -name "project\target" \) -exec rm -Rf {} \;