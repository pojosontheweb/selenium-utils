#!/usr/bin/env bash
if [ -z "$1" ]
  then
	  NODE_PORT=5555
  else
	  NODE_PORT=$1
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

HOST_IP=`/sbin/ip route|awk '/default/ { print $3 }'`

HUB_URL="http://${HOST_IP}:4444/grid/register"
echo "XVFB started, starting node and registering to ${HUB_URL}"

cd /grid
java -Djava.security.egd=file:///dev/urandom -cp *:. -Dwebdriver.chrome.driver=/grid/chromedriver org.openqa.grid.selenium.GridLauncher -role node -hub ${HUB_URL} -maxSession 1 -port ${NODE_PORT} -debug

