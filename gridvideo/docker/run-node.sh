#!/usr/bin/env bash

# args :
# 1 : node port
# 2 : host IP
# 3 : hub url

NODE_PORT=$1
HOST_IP=$2
HUB_URL=$3

echo "Starting XVFB"
/usr/bin/Xvfb :${NODE_PORT} -screen 0 1280x1024x24 +extension RANDR &
ACTIVE=9999
while [ $ACTIVE -ne 0 ] ; do
        xdpyinfo -display :${NODE_PORT} &> /dev/null
        ACTIVE=$?
done
dbus-uuidgen > /var/lib/dbus/machine-id
export CHROME_DEVEL_SANDBOX=
export DISPLAY=:${NODE_PORT}

echo "XVFB started, starting node and registering to ${HUB_URL}"

cd /grid
java -Djava.util.logging.config.file=/grid/grid-logging.properties -Dwebtests.video.dir=/grid/videos -Djava.security.egd=file:///dev/urandom -Dwebdriver.chrome.driver=/grid/chromedriver -cp *:. org.openqa.grid.selenium.GridLauncher -role node -hub ${HUB_URL} -maxSession 1 -port ${NODE_PORT} -host ${HOST_IP} -proxy com.pojosontheweb.selenium.NodeProxy -servlets com.pojosontheweb.selenium.RecorderServlet

