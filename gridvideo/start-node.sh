#!/usr/bin/env bash
sudo docker run -tid -p $1:$1 pojosontheweb/selgrid /grid/run-node.sh $1
