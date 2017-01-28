#!/bin/bash 

echo DB_PORT_3306_TCP_ADDR=${DB_PORT_3306_TCP_ADDR} >> /etc/tomcat/tomcat.conf

cat /etc/tomcat/tomcat.conf
