#!/usr/bin/env bash

sudo docker run -d -p 4444:4444 selgrid /grid/run-hub.sh

if [ -z "$1" ]
  then
          NB_NODES=4
  else
          NB_NODES=$1
fi

PORT=5555
MAX=$[${PORT} + ${NB_NODES} - 1]

for i in `seq ${PORT} ${MAX}`;
do
	sudo docker run -d -p ${i}:${i} selgrid /grid/run-node.sh ${i}
done

echo "Grid started with ${NB_NODES} nodes"
