#!/bin/bash

docker run --name=mysql-server --rm -p 3306:3306 -e MYSQL_ROOT_PASSWORD=toto4242  -v ./mysql:/var/lib/mysql -d mysql:latest