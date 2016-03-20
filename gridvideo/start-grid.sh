#!/usr/bin/env bash

HOST_IP=$1
NB_NODES=$2

if [ -z "$HOST_IP" ]
  then
          echo "Host IP must be provided"
          exit 0
fi

if [ -z "$NB_NODES" ]
  then
          NB_NODES=1
fi

./start-hub.sh

PORT=5555
MAX=$[${PORT} + ${NB_NODES} - 1]

for i in `seq ${PORT} ${MAX}`;
do
	./start-node.sh ${i} ${HOST_IP} $3 $4
done

echo "Grid started with ${NB_NODES} nodes"
