#!/usr/bin/env bash
/usr/bin/Xvfb :99 -screen 0 1024x768x24 +extension RANDR &
taste -d /mnt/target/google -c /mnt/cfg.taste /mnt/google.taste
