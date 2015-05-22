#!/usr/bin/env bash

sudo docker run -d -p 4444:4444 pojosontheweb/selgrid /grid/run-hub.sh

echo "Hub started"
