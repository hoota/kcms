#!/bin/bash

mkdir ~/.m2
mkdir postgresql-data
docker kill $(docker ps | grep kcms-runner | awk '{print $1;}')

docker build -f docker/Dockerfile.build -t kcms-builder:1.0 ./ && \
  docker run -v $(PWD):/opt/app -v $HOME/.m2:/root/.m2 kcms-builder:1.0 && \
  docker build -f docker/Dockerfile.run -t kcms-runner:1.0 ./ && \
  docker run \
    -v $(PWD)/files:/opt/app/files \
    -v $(PWD)/postgresql-data:/var/lib/postgresql/data \
    -v $HOME/.m2:/root/.m2 \
    -p 8080:8080 \
    -d kcms-runner:1.0
