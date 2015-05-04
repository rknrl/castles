#!/bin/sh

wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/7u79-b15/jdk-7u79-linux-x64.tar.gz

mkdir /opt/jdk

tar -zxf jdk-7u79-linux-x64.tar.gz -C /opt/jdk/

update-alternatives --install /usr/bin/java java /opt/jdk/jdk1.7.0_79/bin/java 100

update-alternatives --install /usr/bin/javac javac /opt/jdk/jdk1.7.0_79/bin/javac 100

apt-get update

apt-get install mysql-server

mysql_install_db

/usr/bin/mysql_secure_installation

# create database

install_diamond.sh

mkdir /var/castles
mkdir /var/castles/server
mkdir /var/castles/server/maps

nano /var/castles/my.json
nano /usr/local/bin/castles-start
nano /usr/local/bin/castles-stop
chmod +x /usr/local/bin/castles-start
chmod +x /usr/local/bin/castles-stop

nano /etc/init.d/castles
chmod +x /etc/init.d/castles

update-rc.d castles defaults

scp logback.xml root@dev.rknrl.ru:/var/castles/logback.xml

# todo logback config

service castles start