$env:PATH="$env:PATH;" + (ls "c:\Program Files\Java\*\bin\" | Select -Last 1 -ExpandProperty FullName)
$env:PATH="$env:PATH;" + (ls "c:\bin\apache-maven-*\bin\" | Select -Last 1 -ExpandProperty FullName)
$env:JAVA_HOME=ls "c:\Program Files\Java\*\jre\" | Select -Last 1 -ExpandProperty FullName
$env:MAVEN_HOME=ls "c:\bin\apache-maven-*\" | Select -Last 1 -ExpandProperty FullName