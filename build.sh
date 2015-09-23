#!/bin/bash

##
##  Usage:
##
##     SSH_HOST=user@ip SSH_HOME=/home/user ./build.sh
##  
##  This script:
##  - copies this repository to a docker host
##  - builds a docker image used for building the project
##  - executes ./build.run in a docker container
##

PROJECT_NAME=osgi-tooling
DIR_OUTPUT=${DIR_OUTPUT:=$SSH_HOME/$PROJECT_NAME/output}
DIR_INPUT=${DIR_INPUT:=$SSH_HOME/$PROJECT_NAME/input}
DIR_M2=${DIR_M2:=$SSH_HOME/.m2}
DIR_IVY2=${DIR_IVY2:=$SSH_HOME/.ivy2}
DIR_SBT_BOOT=${DIR_SBT_BOOT:=$SSH_HOME/.sbt/boot}
IMAGE_NAME=${IMAGE_NAME:=wav-sbt-build:latest}
CONTAINER_NAME=${CONTAINER_NAME:=$PROJECT_NAME}
SSH="ssh $SSH_HOST"

if [[ "$SSH_HOST" = "" ]]; then
  echo "SSH_HOST is not set, try: SSH_HOST=user@ip" >&2
  exit 1
fi

if [[ "$SSH_HOME" = "" ]]; then
  echo "SSH_HOME is not set, try: SSH_HOME=/home/USER" >&2
  exit 1
fi

if [[ ! -d .git ]]; then
    echo "This must be run at the base of the git repository" >&2
  exit 1
fi

echo "Destroying existing docker container named $CONTAINER_NAME"
$SSH docker rm -f $CONTAINER_NAME

$SSH sudo rm -Rf $DIR_OUTPUT $DIR_INPUT
$SSH mkdir -p $DIR_OUTPUT $DIR_INPUT $DIR_M2 $DIR_IVY2 $DIR_SBT_BOOT
if [[ $? -ne 0 ]]; then
    echo Failed to mkdir the working directories for the build on docker host >&2
    exit 1
fi

git ls-files > files.txt

echo Extracting to $DIR_INPUT
tar czf - --files-from files.txt | $SSH tar xzf - -C $DIR_INPUT
if [[ $? -ne 0 ]]; then
    echo Failed copy the tracked files in this git repository to the input directory in the docker host >&2
    exit 1
fi

$SSH docker version > env.txt

$SSH docker build -t $IMAGE_NAME $DIR_INPUT/.
if [[ $? -ne 0 ]]; then
    echo Failed to build the docker context $DIR_INPUT/. >&2
    exit 1
fi

echo "Building in a docker container named $CONTAINER_NAME"
echo [OUTPUT]

$SSH docker run --rm --name $CONTAINER_NAME \
    -v $DIR_OUTPUT:/root/output \
    -v $DIR_INPUT:/root/input \
    -v $DIR_M2:/root/.m2 \
    -v $DIR_IVY2:/root/.ivy2 \
    -v $DIR_SBT_BOOT:/root/.sbt/boot \
    $IMAGE_NAME /root/input/build.run

if [[ $? -ne 0 ]]; then
    echo The build has failed $DIR_INPUT/. >&2
    exit 1
fi