#!/bin/bash

set -exv

if [[ $(git --no-pager  log --oneline -1) == *Bump* ]]; then
  echo "This is a dependabot bump, let's ignore this commit."
  exit 0
fi

./deploy/build_deploy_community_app.sh;
./deploy/build_deploy_acceptance_test.sh;
