#!/bin/bash

set -exv

oc process -f code-quarkus.yaml | oc apply -f -;
HOST=$(oc get route code-quarkus-frontend -o json | jq -r '.spec.host');
oc expose svc/code-quarkus;
