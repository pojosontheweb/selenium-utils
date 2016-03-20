#!/usr/bin/env bash

# args :
# 1 : node port
# 2 : host IP

NODE_PORT=$1
HOST_IP=$2
HUB_URL="http://${HOST_IP}:4444/grid/register"

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
java -Djava.util.logging.config.file=/grid/grid-logging.properties -Dwebtests.video.dir=/grid/videos -Djava.security.egd=file:///dev/urandom -Dwebdriver.chrome.driver=/grid/chromedriver -cp *:. org.openqa.grid.selenium.GridLauncher -role node -hub ${HUB_URL} -maxSession 1 -port ${NODE_PORT} -host ${HOST_IP} -proxy com.pojosontheweb.selenium.NodeProxy -servlets com.pojosontheweb.selenium.RecorderServlet

