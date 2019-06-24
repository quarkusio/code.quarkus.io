#!/bin/bash

REVISION=$(echo $(git show --format=%H -s HEAD) | cut -c1-7)

docker build ./src/main/docker/Dockerfile.jvm -t fabric8/launcher-quarkus:latest .
docker tag fabric8/launcher-quarkus:latest fabric8/launcher-quarkus:$REVISION
docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
docker push fabric8/launcher-quarkus:latest
docker push fabric8/launcher-quarkus:$REVISION
