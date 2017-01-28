#!/bin/bash 

#package the WAR file
mvn package

docker build -t samyjin:dev .

#create network
docker network create my-network

docker run --net=my-network --name=mysql-server --rm -p 3306:3306 -e MYSQL_ROOT_PASSWORD=toto4242  -v c:/docker_project/mysql:/var/lib/mysql -d mysql:latest

#run the application
docker run --net=my-network --name=tomcat_server --rm -p 8080:8080 samyjin:dev
