#!/usr/bin/env bash
cd /grid
java -Djava.security.egd=file:///dev/urandom -cp *:. org.openqa.grid.selenium.GridLauncher -role hub -debug
