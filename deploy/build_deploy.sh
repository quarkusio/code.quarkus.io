#!/bin/bash

set -exv

if [[ $(git --no-pager  log --oneline -1) == *Bump* ]]; then
  exit 1
fi
cd ..;
./deploy/build_deploy_community-app.sh
./deploy./build_deploy_acceptance_test.sh
