#!/usr/bin/env bash
sudo docker run -d -p $1:$1 pojosontheweb/selgrid /grid/run-node.sh $1
