#!/usr/bin/env bash
mvn clean install -Dwebdriver.chrome.driver=$CHROMEDRIVER -Dwebtests.browser=chrome
