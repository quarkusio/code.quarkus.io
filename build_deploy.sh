#!/bin/bash

set -exv

if [[ $(git --no-pager  log --oneline -1) == *Bump* ]]; then
  exit 1
fi

(cd frontend;./build_deploy.sh)
(cd api;./build_deploy.sh)
(cd acceptance-test;./build_deploy.sh)