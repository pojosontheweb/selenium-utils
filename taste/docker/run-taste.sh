#!/usr/bin/env bash
echo "Starting XVFB"
/usr/bin/Xvfb :99 -screen 0 1024x768x24 +extension RANDR &
ACTIVE=9999
while [ $ACTIVE -ne 0 ] ; do
        xdpyinfo -display :99 &> /dev/null
        ACTIVE=$?
done
echo "XVFB started, calling taste executable"
taste $*
echo "Killing XVFB"
kill `pidof Xvfb`
echo "Done"
