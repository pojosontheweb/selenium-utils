FROM nimmis/java-centos:openjdk-7-jdk

# xvfb

RUN yum -y upgrade
RUN yum -y install xorg-x11-server-Xvfb libXtst xdpyinfo
ENV DISPLAY :99
ENV LD_LIBRARY_PATH=/usr/lib64/

# firefox

RUN yum -y install firefox

# chrome
# TODO

# Taste

ADD /taste-1.0-beta4-bin.tar.gz /
ENV TASTE_HOME /taste
ENV PATH $PATH:$TASTE_HOME/bin

ADD /run-taste.sh /
RUN chmod +x /run-taste.sh


