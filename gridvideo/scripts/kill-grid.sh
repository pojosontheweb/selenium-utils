#!/usr/bin/env bash
sudo docker ps | grep selgrid | awk '{ print $1 }' | xargs --no-run-if-empty sudo docker rm -f
