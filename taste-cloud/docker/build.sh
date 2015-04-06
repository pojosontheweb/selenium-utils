#!/usr/bin/env bash
TARGET=../target
DOCKER_TARGET=$TARGET/docker
mkdir $DOCKER_TARGET
cp Dockerfile $DOCKER_TARGET
cp $TARGET/taste-cloud.war $DOCKER_TARGET
docker build -t taste-cloud $DOCKER_TARGET
