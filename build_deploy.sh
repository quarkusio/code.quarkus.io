#!/bin/bash

set -exv

GIT_REV=$(git rev-parse --short=7 HEAD)
IMAGE=${IMAGE-"quay.io/quarkus/code-quarkus-app"}
IMAGE_TAG=${IMAGE_TAG-$GIT_REV}
MAVEN_EXTRA_ARGS=${MAVEN_EXTRA_ARGS-""}
NATIVE_BUILD_MEMORY=${$NATIVE_BUILD_MEMORY-"4g"}

docker build --compress -f src/main/docker/Dockerfile.native.multistage  --build-arg NATIVE_BUILD_MEMORY="$NATIVE_BUILD_MEMORY" MAVEN_EXTRA_ARGS="$MAVEN_EXTRA_ARGS -Dgit.commit.id=$GIT_REV" -t "${IMAGE}:${IMAGE_TAG}" .

if [[ -n "$QUAY_USER" && -n "$QUAY_TOKEN" ]]; then
    DOCKER_CONF="$PWD/.docker"
    mkdir -p "$DOCKER_CONF"
    docker tag "${IMAGE}:${IMAGE_TAG}" "${IMAGE}:latest"
    docker --config="$DOCKER_CONF" login -u="$QUAY_USER" -p="$QUAY_TOKEN" quay.io
    docker --config="$DOCKER_CONF" push "${IMAGE}:${IMAGE_TAG}"
    docker --config="$DOCKER_CONF" push "${IMAGE}:latest"
fi
