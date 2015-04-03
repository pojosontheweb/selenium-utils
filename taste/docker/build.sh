#!/usr/bin/env bash
TARGET=../target
DOCKER_TARGET=$TARGET/docker
mkdir $DOCKER_TARGET
cp Dockerfile $DOCKER_TARGET
cp run-taste.sh $DOCKER_TARGET
cp $TARGET/taste-1.0-beta4-bin.tar.gz .$DOCKER_TARGET
sudo docker build -t taste $DOCKER_TARGET