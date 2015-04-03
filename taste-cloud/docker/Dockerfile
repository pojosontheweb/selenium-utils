FROM centos
MAINTAINER VANKEISB@fr.ibm.com

RUN yum -y install vim wget tar unzip

RUN wget http://i2a-icct1.parislab.fr.ibm.com/VTT/bundles/JDK/linux/jdk-7u76-fcs-bin-b13-linux-x64-18_dec_2014.zip
RUN mkdir -p /opt/oracle-jdk7;cd /opt/oracle-jdk7;unzip /jdk-7u76-fcs-bin-b13-linux-x64-18_dec_2014.zip
RUN cd /opt/oracle-jdk7; mv jdk7_76/jdk1.7.0_76/* .; rm -rf jdk7_76

ENV JAVA_HOME /opt/oracle-jdk7

RUN wget http://www.interior-dsgn.com/apache/tomcat/tomcat-8/v8.0.20/bin/apache-tomcat-8.0.20.tar.gz
RUN tar zxf apache-tomcat-8.0.20.tar.gz

RUN rm apache-tomcat-8.0.20.tar.gz
RUN rm jdk-7u76-fcs-bin-b13-linux-x64-18_dec_2014.zip

RUN rm -rf /apache-tomcat-8.0.20/webapps/*
ADD taste-cloud.war /apache-tomcat-8.0.20/webapps/ROOT.war

EXPOSE 8080
ENTRYPOINT /apache-tomcat-8.0.20/bin/catalina.sh run

