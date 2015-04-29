#!/bin/sh

sudo apt-get install make pbuilder python-mock python-configobj python-support cdbs

cd /var

git clone https://github.com/BrightcoveOS/Diamond.git

cd Diamond

make builddeb

dpkg -i build/diamond_4.0.57_all.deb

scp diamond.conf root@graph.rknrl.ru:/etc/diamond/diamond.conf

service diamond start