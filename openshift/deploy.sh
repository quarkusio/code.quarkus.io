#!/bin/bash

set -exv

oc process -f code-quarkus-frontend.yaml  | oc apply -f -;
oc process -f code-quarkus-api.yaml | oc apply -f -;
oc expose svc/code-quarkus-frontend;
HOST=$(oc get route code-quarkus-frontend -o json | jq -r '.spec.host');
oc expose svc/code-quarkus-api --hostname=$HOST --path='/api';
oc expose svc/code-quarkus-api --name=code-quarkus-api-d --hostname=$HOST --path='/q';
oc expose svc/code-quarkus-api --name=code-quarkus-api-q --hostname=$HOST --path='/d';