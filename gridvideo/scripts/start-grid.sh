#!/usr/bin/env bash

# args :
# 1 : nb nodes
# 2 : video dir on the host
# 3 : host IP

./start-hub.sh

# arg 1 is the nb of nodes we want
if [ -z "$1" ]
  then
  	NB_NODES=4
  else
    NB_NODES=$1
fi

if [ -z "$2" ]
  then
    VIDEO_DIR="/tmp"
  else
  	VIDEO_DIR=$2
fi

if [ -z "$3" ]
  then
	HOST_IP=`/sbin/ip route|awk '/default/ { print $3 }'`
  else
  	HOST_IP=$3
fi

PORT=5555
MAX=$[${PORT} + ${NB_NODES} - 1]

for i in `seq ${PORT} ${MAX}`;
do
	./start-node.sh ${i} ${VIDEO_DIR} ${HOST_IP}
done

echo "Grid started with ${NB_NODES} nodes, video dir=${VIDEO_DIR}, hostIP=${HOST_IP}"
