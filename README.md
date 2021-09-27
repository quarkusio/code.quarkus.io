# Code Quarkus App

 [![Code Quarkus API CI](https://github.com/quarkusio/code.quarkus.io/actions/workflows/code-quarkus-api-actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/code-quarkus-api-actions.yml) [![Code Quarkus Frontend CI](https://github.com/quarkusio/code.quarkus.io/actions/workflows/code-quarkus-frontend-actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/code-quarkus-frontend-actions.yml)
 [![Code Quarkus Library CI](https://github.com/quarkusio/code.quarkus.io/actions/workflows/code-quarkus-library-publish-actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/code-quarkus-library-publish-actions.yml)

# Development

Have a look to the Makefile to find the most used dev commands.

In one terminal tab, use this command to start backend in dev mode (on: http://0.0.0.0:8080).
```
make dev-api
```

Or use those command to start the api in packaged mode (on: http://0.0.0.0:8080).
```
make build-api
make start-api
```

In the another terminal tab, use those commands to link the library and then start frontend in dev mode also watching for changes in the lib (on: http://0.0.0.0:3000).
```
make link-lib
make dev-lib
```

If you want to unlink the local library and use the npm package library in the frontend:
```
make unlink-lib
```

# Publishing a change in the library to npm

## @quarkusio/code-quarkus library NPM packages

- https://www.npmjs.com/package/@quarkusio/code-quarkus.components
- https://www.npmjs.com/package/@quarkusio/code-quarkus.core.analytics
- https://www.npmjs.com/package/@quarkusio/code-quarkus.core.components
- https://www.npmjs.com/package/@quarkusio/code-quarkus.core.types



On your PR which contains the changes, run (before committing):
```
make tag-lib
```

It will automatically change the `.bitmap` file, which will trigger a GH action when the PR gets merged to publish a new version in npm.

# Staging

Staging is auto-updated with main (it takes ~15min to refresh after a merge): https://stage.code.quarkus.io

You can check deployed commit hash on: https://stage.code.quarkus.io/api/config

# Promote to production (temporarily only available to Red Hat developers)

1. Check that everything works as expected on [staging](#staging)
2. (Inside Red Hat private network) Create a PR on this [link](https://gitlab.cee.redhat.com/service/app-interface/-/edit/master/data/services/quarkus/cicd/ci-ext/saas.yaml) with the commit hash to release in the production `ref: ...` **(there are two `ref` to edit, api & frontend)**
3. Comment with `/lgtm` and wait for CI checks
4. Merging the PR will trigger a deployment to production

# API Documentation

- [OpenApi](https://code.quarkus.io/q/swagger-ui)

## Infra Managed by Red Hat app-sre team 

All those links have restricted access:

- Staging cluster: https://console-openshift-console.apps.app-sre-stage-0.k3s7.p1.openshiftapps.com/k8s/cluster/projects
- Production cluster: https://console-openshift-console.apps.app-sre-prod-01.i7w5.p1.openshiftapps.com/k8s/cluster/projects/code-quarkus-production
- CI/CD: https://ci.ext.devshift.net/view/quarkus/
- Jira: https://issues.redhat.com/projects/APPSRE/issues/
- Infra repository: https://gitlab.cee.redhat.com/service/app-interface
- Slack: https://coreos.slack.com (channel sd-app-sre)

To request access, please contact adamevin@redhat.com
