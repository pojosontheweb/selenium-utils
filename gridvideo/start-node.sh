#!/usr/bin/env bash
docker run --net=host -tid -p $1:$1 pojosontheweb/selgrid /grid/run-node.sh $1 $2 $3 $4
