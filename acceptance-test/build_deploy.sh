#!/bin/bash

set -exv

GIT_REV_SHORT=$(git rev-parse --short=7 HEAD)
IMAGE=${IMAGE-"quay.io/quarkus/code-quarkus-acceptance-test"}
IMAGE_TAG=${IMAGE_TAG-$GIT_REV_SHORT}

docker build --compress -f src/main/docker/Dockerfile.multistage -t "${IMAGE}:${IMAGE_TAG}" .

if [[ -n "$QUAY_USER" && -n "$QUAY_TOKEN" ]]; then
    DOCKER_CONF="$PWD/.docker"
    mkdir -p "$DOCKER_CONF"
    docker tag "${IMAGE}:${IMAGE_TAG}" "${IMAGE}:latest"
    echo "$QUAY_TOKEN" | docker --config="$DOCKER_CONF" login -u="$QUAY_USER" --password-stdin quay.io
    docker --config="$DOCKER_CONF" push "${IMAGE}:${IMAGE_TAG}"
    docker --config="$DOCKER_CONF" push "${IMAGE}:latest"
fi
