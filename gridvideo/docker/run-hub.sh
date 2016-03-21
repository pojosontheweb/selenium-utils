#!/usr/bin/env bash
cd /grid
java -Djava.util.logging.config.file=/grid/grid-logging.properties -Dwebtests.video.dir=/grid/videos -Djava.security.egd=file:///dev/urandom -cp *:. org.openqa.grid.selenium.GridLauncher -role hub -servlets com.pojosontheweb.selenium.FrontEndServlet
