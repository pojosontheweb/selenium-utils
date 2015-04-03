#!/usr/bin/env bash
TARGET=../../target/docker
mkdir $TARGET
cp Dockerfile $TARGET
cp run-taste.sh $TARGET
cp $TARGET/taste-1.0-beta4-bin.tar.gz .$TARGET/docker
sudo docker build -t taste $TARGET/docker