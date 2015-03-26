#!/usr/bin/env bash
mkdir ../target/docker
cp Dockerfile ../target/docker
cp run.sh ../target/docker
cp ../target/taste-1.0-beta4-bin.tar.gz ../target/docker
sudo docker build -t taste ../target/docker