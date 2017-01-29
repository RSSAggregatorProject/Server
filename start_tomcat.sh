#!/bin/bash 
bdd_path="c:/docker_project/mysql"

#package the WAR file
mvn package

docker build -t samyjin:dev .

#create network
docker network create --driver bridge my-network-bridge 

docker run --net=my-network-bridge --name=mysql-server --rm -p 3306:3306 -e MYSQL_ROOT_PASSWORD=toto4242  -v $bdd_path:/var/lib/mysql -d mysql:latest

#run the application
docker run --net=my-network-bridge --name=tomcat_server --rm -p 8080:8080 samyjin:dev
