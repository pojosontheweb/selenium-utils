#!/usr/bin/env bash
docker run -d -p $1:$1 selgrid /grid/run-node.sh $1
