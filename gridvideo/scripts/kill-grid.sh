#!/usr/bin/env bash
docker rm -f $(docker ps | grep selgrid | awk '{ print $1 }')
