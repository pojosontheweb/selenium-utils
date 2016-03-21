#!/usr/bin/env bash

# arg 1 is the session ID
if [ -z "$1" ]
  then
  	echo "sessionId is mandatory"
  	exit 0
  else
    SESSION_ID=$1
fi

# arg 2 is the IP of the hub
if [ -z "$2" ]
  then
  	HUB_URL=localhost
  else
    HUB_URL=$2
fi

curl "http://${HUB_URL}:4444/grid/admin/FrontEndServlet?command=download&sessionId=${SESSION_ID}"
