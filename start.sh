#!/bin/bash 

#package the WAR file
mvn package

#run the application
docker run --rm -p 8080:8080 -v //./target/mywebapp:/usr/local/tomcat/webapps/mywebapp tomcat:9.0
