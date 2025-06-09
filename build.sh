#!/bin/bash

mkdir ~/.m2
mkdir postgresql
chmod 777 postgresql

CONTAINER_NAME=kcms-project

docker build -f docker/Dockerfile.build -t kcms-builder:1.0 ./ && \
  docker run -v $PWD:/opt/app -v $HOME/.m2:/root/.m2 kcms-builder:1.0 && \
  docker build -f docker/Dockerfile.run -t $CONTAINER_NAME:1.0 ./ && \
  docker stop $(docker ps | grep $CONTAINER_NAME | awk '{print $1;}')

docker container prune -f

docker run \
  -v $PWD/files:/opt/app/files \
  -v $PWD/postgresql:/var/lib/postgresql \
  -v $HOME/.m2:/root/.m2 \
  -p 8080:8080 \
  --name $CONTAINER_NAME \
  -d --restart=unless-stopped $CONTAINER_NAME:1.0

docker ps