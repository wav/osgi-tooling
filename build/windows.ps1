if ( -not (Test-Path -PathType Container -Path .git ) ) {
    Write-Error "This must be run at the base of the git repository" 
    Exit 1
}

. build\get-sbt.ps1

. build\util.ps1

if ( -not (Test-Path -PathType Container -Path "karaf.zip" ) ) {
    mvnGet "org.apache.karaf:apache-karaf-minimal:4.0.2:zip" "karaf.zip"
}

unzipToOutputDir "$pwd/karaf.zip" "$pwd/karaf"

if ( -not (Test-Path -PathType Container -Path "$pwd/karaf" )) {
    Write-Error "Failed to extract karaf.zip to karaf"
    Exit 1
}

# `build.commands` is list of sbt commands to be run
$nl=[System.Environment]::NewLine
$commands=";" + ((Get-Content build.commands) -join ";").Replace($nl,";").Trim(";")

echo "bin/sbt $commands"

java -XX:+CMSClassUnloadingEnabled -jar "$pwd\bin\sbt-launcher.jar" "$commands"