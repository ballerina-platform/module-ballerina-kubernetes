#!/usr/bin/env bash

docker build -t hotdrink_mysql_db:1.0.0 ./hotdrinkdb/
docker build -t cooldrink_mysql_db:1.0.0 ./cooldrinkdb/

kubectl create namespace mysql
kubectl create -f ./mysql-deployment.yaml