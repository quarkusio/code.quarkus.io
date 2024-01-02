#!/bin/bash

set -exv

if [[ $(git --no-pager  log --oneline -1) == *Bump* ]]; then
  exit 1
fi

./deploy/build_deploy_community_app.sh;
./deploy/build_deploy_acceptance_test.sh;
