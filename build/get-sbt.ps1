$sbtVersion=Get-Content .\project\build.properties | Select-String -AllMatches "^sbt.version=(.*)" | ForEach-Object { $_.Matches.Groups[1].Value }
$sbtFile="$pwd\bin\sbt-launcher.jar"
$sbtUrl="http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$sbtVersion/sbt-launch.jar"

mkdir -f "$pwd\bin" | Out-Null

if ( -not (Test-Path -PathType Leaf -Path $sbtFile) ) {
    try {
        (New-Object System.Net.WebClient).DownloadFile($sbtUrl, $sbtFile)
    } catch {
        Write-Error $_.Exception
    }
}

if ( -not (Test-Path -PathType Leaf -Path $sbtFile) ) {
    Write-Error "Failed to download $sbtUrl to $sbtFile"
    Exit 1
}