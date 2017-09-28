FROM selenium/node-chrome:3.5.3-boron

## xvfb
#ENV DISPLAY :99
#ENV LD_LIBRARY_PATH /usr/lib64/

RUN  sudo mkdir -p /grid/videos \
  && sudo chown -R seluser:seluser /grid

# selenium grid
ADD http://selenium-release.storage.googleapis.com/2.45/selenium-server-standalone-2.45.0.jar /grid/selenium-server-standalone-2.45.0.jar
ADD /run-node.sh /grid/run-node.sh
ADD /run-hub.sh /grid/run-hub.sh
ADD /grid-logging.properties /grid/grid-logging.properties

ADD /selenium-utils-gridvideo-LATEST-SNAPSHOT-jar-with-dependencies.jar /grid/selenium-utils-gridvideo-LATEST-SNAPSHOT-jar-with-dependencies.jar

RUN sudo chown -R seluser:seluser /grid



