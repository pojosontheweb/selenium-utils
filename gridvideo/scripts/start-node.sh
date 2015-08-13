#!/usr/bin/env bash
sudo docker run --net=host -tid -p $1:$1 -v $2:/grid/videos pojosontheweb/selgrid /grid/run-node.sh $1 $2 $3
