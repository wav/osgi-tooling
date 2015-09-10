#!/bin/bash

OUTDIR=./.out

mvnGet() {
    [[ -f $2 ]] && return 0
    echo Downloading: $1 -> $2
    mvn dependency:get \
            -Dsilent=true \
            -Dartifact=$1 \
            -Dtransitive=false \
        dependency:copy \
            -Dsilent=true \
            -Dartifact=$1 \
            -DoutputDirectory=./.out
    local f=`ls $OUTDIR`
    mv $OUTDIR/$f ./$2
}

tarFromOutDir() {
    if  [ "$(ls -A $OUTDIR 2> /dev/null)" == "" ] && \
        [ "$(ls -A ./$2 2> /dev/null)" == "" ]; then
        tar xf $1 -C $OUTDIR
        local d=`ls $OUTDIR`
        mv $OUTDIR/$d ./$2
    fi
}

buildImage() {
    local tag=`cat tag`
    [[ "$tag" == "" ]] && \
        echo Could not read the tag name from "tag" >&2 && \
        exit 1
    docker build -t "$tag" .
}