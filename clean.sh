#!/bin/bash

rm -Rf ~/.ivy2/local/wav.devtools/* \ 
	~/.sbt/0.13/{dependency,staging}
find . \( -iname ".DS_Store" -o -name "target" -o -name "project\target" \) -exec rm -Rf {} \; 2> /dev/null