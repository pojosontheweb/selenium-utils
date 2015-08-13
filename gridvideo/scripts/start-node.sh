#!/usr/bin/env bash

# args :
# 1 : node port
# 2 : video dir
# 3 : host IP

mkdir -p $2

sudo docker run --net=host -tid -p $1:$1 -v $2:/grid/videos pojosontheweb/selgrid /grid/run-node.sh $1 $3

echo "Node started on port $1, videoDir=$2, hostIp=$3"