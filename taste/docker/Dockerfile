FROM centos:centos7

ENV JAVA_VERSION 7u75
ENV BUILD_VERSION b13

# Upgrading system
RUN yum -y upgrade
RUN yum -y install wget

# Downloading Java
RUN wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/$JAVA_VERSION-$BUILD_VERSION/jdk-$JAVA_VERSION-linux-x64.rpm" -O /tmp/jdk-linux-x64.rpm

RUN yum -y install /tmp/jdk-linux-x64.rpm

RUN alternatives --install /usr/bin/java jar /usr/java/latest/bin/java 200000
RUN alternatives --install /usr/bin/javaws javaws /usr/java/latest/bin/javaws 200000
RUN alternatives --install /usr/bin/javac javac /usr/java/latest/bin/javac 200000

ENV JAVA_HOME /usr/java/latest

ADD /taste-1.0-beta4-bin.tar.gz /

ENV TASTE_HOME /taste
ENV PATH $PATH:$TASTE_HOME/bin

ADD /run.sh /
RUN chmod +x /run.sh

RUN echo 'Taste is ready to rock !'

# xvfb
RUN yum -y install xorg-x11-server-Xvfb libXtst
ENV DISPLAY :99
ENV LD_LIBRARY_PATH=/usr/lib64/

# firefox
RUN yum -y install firefox

# chrome
# TODO

# startup xvfb
CMD ["/usr/bin/Xvfb", ":99", "-screen", "0", "1024x768x24", "+extension", "RANDR"]

