#!/bin/bash

set -exv

(cd frontend;./build_deploy.sh)
(cd api;./build_deploy.sh)
(cd acceptance-test;./build_deploy.sh)