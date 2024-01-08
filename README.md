# Code Quarkus App

 [![Tests](https://github.com/quarkusio/code.quarkus.io/actions/workflows/build.actions.yml/badge.svg)](https://github.com/quarkusio/code.quarkus.io/actions/workflows/build.actions.yml)

# Development

Api is located in `base/src/main/java`
UI is located in `base/src/main/resources/web/`

Use this command to start `community-app` dev mode on: http://0.0.0.0:8080 (Api and UI).
```
cd base && quarkus dev
```

Build the app locally:
```
mvn clean install -Dlib -Dcommunity-app
```

After building, use this command to start the `community-app` in production mode:
```
java -jar community-app/target/quarkus-app/quarkus-run.jar
```

# Staging

Staging is auto-updated with main (it takes ~15min to refresh after a merge): https://stage.code.quarkus.io

You can check deployed commit hash on: https://stage.code.quarkus.io/api/config

# Promote to production

It is automated based on the [acceptance tests](https://github.com/quarkusio/code.quarkus.io/tree/main/acceptance-test)

# API Documentation

- [OpenApi (Swagger UI)](https://editor.swagger.io/?url=https://code.quarkus.io/q/openapi)
