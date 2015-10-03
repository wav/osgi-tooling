$OUTDIR="$pwd/.out"

mkdir -f "$OUTDIR"

[System.Reflection.Assembly]::LoadWithPartialName("System.IO.Compression.FileSystem") | Out-Null

function mvnGet([string]$coords, [string]$path) {
    if ( Test-Path -PathType Leaf -Path $path ) {
        return true
    }
    mvn dependency:get `
            -Dsilent=true `
            -Dartifact="$coords" `
            -Dtransitive=false `
        dependency:copy `
            -Dsilent=true `
            -Dartifact="$coords" `
            -DoutputDirectory="$OUTDIR"
    $f=ls "$OUTDIR" | Select -First 1 -ExpandProperty Name
    mv "$OUTDIR\$f" "$path"
}

function unzipToOutputDir([string]$archivePath, [string]$targetPath) {
    if ( -not (Test-Path -PathType Leaf -Path "$archivePath") ) {
        Write-Error "$archivePath does not exist"
        return false
    }
    if ( Test-Path -PathType Container -Path "$targetPath" ) {
        Write-Debug "$targetPath exists, skipping"
        return true
    }
    [System.IO.Compression.ZipFile]::ExtractToDirectory("$archivePath", "$OUTDIR")
    $d=ls "$OUTDIR" | Select -First 1 -ExpandProperty Name
    mv "$OUTDIR\$d" "$targetPath"
}