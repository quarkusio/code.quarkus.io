# Code Quarkus App

 [![API Tests](https://github.com/quarkusio/code.quarkus.io/actions/workflows/api.tests.actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/api.tests.actions.yml) [![Frontend Tests](https://github.com/quarkusio/code.quarkus.io/actions/workflows/frontend.tests.actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/frontend.tests.actions.yml)
 [![Code Quarkus Library CI](https://github.com/quarkusio/code.quarkus.io/actions/workflows/library.publish.actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/library.publish.actions.yml)

# Development

> Have a look at the Makefile to find the most used dev commands.

Link the library for dev:
```
make link-lib
```

Use this command to start dev mode (on: http://0.0.0.0:8080).
```
make dev
```

Or use those command to start the api in packaged mode (on: http://0.0.0.0:8080).
```
make build-api
make start-api
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

# Promote to production

It is automated based on the [acceptance tests](https://github.com/quarkusio/code.quarkus.io/tree/main/acceptance-test)

# API Documentation

- [OpenApi (Swagger UI)](https://editor.swagger.io/?url=https://code.quarkus.io/q/openapi)

