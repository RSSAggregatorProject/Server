#!/bin/bash 

#package the WAR file
mvn package

docker build -t samyjin:dev .

#run the application
docker run --name tomcat_server --rm -p 8080:8080 samyjin:dev
