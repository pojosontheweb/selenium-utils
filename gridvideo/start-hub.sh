#!/usr/bin/env bash

sudo docker run --net=host -tid -p 4444:4444 pojosontheweb/selgrid /grid/run-hub.sh

echo "Hub started"
