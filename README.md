# Code Quarkus App

 [![Tests](https://github.com/quarkusio/code.quarkus.io/actions/workflows/build.actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/build.actions.yml) [![Version](https://img.shields.io/maven-central/v/io.quarkus.code/code-quarkus?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkus.code/code-quarkus)

The structure of this Web app is a bit special as it is possible to extend and customize it. It is what we could call a Fullstack React library thanks to the power of the [Quarkus Web Bundler](https://github.com/quarkiverse/quarkus-web-bundler). It also contains our community implementation for code.quarkus.io.

📁  **./web-deps** contains all the web dependencies for the library, it is split in its own module to allow the library to be used in other Quarkus apps.

📁  **./base** is the base library, it contains the Api and the Code Quarkus React library, it also contains the Community App for dev purposes (you can remove it if you provide your own).

📁  **./community-app** is the community-app Quarkus app, it uses the base library without doing much as it directly use the Community App inside it.

📁  **./acceptance-test** are the test which run to auto promote new versions to production

https://github.com/redhat-developer/code.quarkus.redhat.com is an example of how this can be extended and customized.

# Development

Install [https://github.com/casey/just](just)

Api is located in `base/src/main/java`
UI is located in `base/src/main/resources/web/`

Use this command to start `community-app` dev mode on: http://0.0.0.0:8080 (Api and UI).
```shell
# Install the parent pom and web-deps
just deps

# Start the dev mode
just dev
```

Build the app locally:
```shell
just build
```

After building, use this command to start the `community-app` in production mode:
```shell
just start
```

# Staging

Staging is auto-updated with main (it takes ~15min to refresh after a merge): https://stage.code.quarkus.io

You can check deployed commit hash on: https://stage.code.quarkus.io/api/config

# Promote to production

It is automated based on the [acceptance tests](https://github.com/quarkusio/code.quarkus.io/tree/main/acceptance-test)

# API Documentation

- [OpenApi (Swagger UI)](https://editor.swagger.io/?url=https://code.quarkus.io/q/openapi)
