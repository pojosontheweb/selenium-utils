#!/usr/bin/env bash
docker ps | grep selgrid | awk '{ print $1 }' | xargs --no-run-if-empty docker rm -f
