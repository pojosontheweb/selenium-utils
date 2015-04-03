FROM centos-ibmjdk7

MAINTAINER VANKEISB@fr.ibm.com

RUN yum -y install vim wget tar unzip

RUN wget http://www.interior-dsgn.com/apache/tomcat/tomcat-8/v8.0.20/bin/apache-tomcat-8.0.20.tar.gz
RUN tar zxf apache-tomcat-8.0.20.tar.gz
RUN rm apache-tomcat-8.0.20.tar.gz

RUN rm -rf /apache-tomcat-8.0.20/webapps/*
ADD taste-cloud.war /apache-tomcat-8.0.20/webapps/ROOT.war

EXPOSE 8080
CMD /apache-tomcat-8.0.20/bin/catalina.sh run

