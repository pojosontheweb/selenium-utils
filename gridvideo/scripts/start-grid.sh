#!/usr/bin/env bash

./start-hub.sh

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

PORT=5555
MAX=$[${PORT} + ${NB_NODES} - 1]

for i in `seq ${PORT} ${MAX}`;
do
	./start-node.sh ${i} ${VIDEO_DIR}
done

echo "Grid started with ${NB_NODES} nodes, video dir= ${VIDEO_DIR}"
