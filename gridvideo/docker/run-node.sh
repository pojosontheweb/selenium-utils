#!/usr/bin/env bash

if [ -z "$1" ]
  then
	NODE_PORT=5555
  else
	NODE_PORT=$1
fi

if [ -z "$2" ]
  then
    HOST_IP=`/sbin/ip route|awk '/default/ { print $3 }'`
  else
    HOST_IP=$2
fi

if [ -z "$3" ]
  then
    HUB_URL="http://${HOST_IP}:4444/grid/register"
  else
    HUB_URL=$3
fi

if [ -z "$4" ]
  then
    MAX_SESSIONS=1
  else
    MAX_SESSIONS=$4
fi

echo "Starting XVFB"
/usr/bin/Xvfb :99 -screen 0 1024x768x24 +extension RANDR &
ACTIVE=9999
while [ $ACTIVE -ne 0 ] ; do
        xdpyinfo -display :99 &> /dev/null
        ACTIVE=$?
done
dbus-uuidgen > /var/lib/dbus/machine-id
export CHROME_DEVEL_SANDBOX=

echo "XVFB started, starting node and registering to ${HUB_URL}"

cd /grid
java -Djava.security.egd=file:///dev/urandom -cp *:. -Dwebdriver.chrome.driver=/grid/chromedriver org.openqa.grid.selenium.GridLauncher -role node -hub ${HUB_URL} -maxSession ${MAX_SESSIONS} -port ${NODE_PORT} -debug -host ${HOST_IP}

