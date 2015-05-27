#!/usr/bin/env bash

sudo docker run -tid -p 4444:4444 pojosontheweb/selgrid /grid/run-hub.sh

echo "Hub started"
