#!/usr/bin/env bash
cd /grid
java -Djava.util.logging.config.file=/grid/grid-logging.properties -Djava.security.egd=file:///dev/urandom -cp *:. org.openqa.grid.selenium.GridLauncher -role hub -debug
