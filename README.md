# Code Quarkus App

# Backend dev

```
$ ./mvnw compile quarkus:dev
```

# Frontend dev

Open `./src/main/frontend` with your favorite IDE and use `yarn`:
```
$ yarn && yarn start
```

(the frontend needs the backend to be running on http://localhost:8080)

# Staging

Staging is auto-updated with master (it takes 15min to refresh after a merge): https://stage.code.quarkus.io

You can check deployed commit hash on: https://stage.code.quarkus.io/api/config

# Promote to production

1. Check that everything works as expected on [staging](#staging)
2. Create a PR on https://github.com/quarkusio/code.quarkus.io-release with the commit hash to release
3. Merging the PR will trigger a deployment on production

# To update the Quarkus version (after a new Quarkus release)

1. (Optional) Edit the `pom.xml` with the new Quarkus version (Only used to bundle Code Quarkus): `<version.quarkus>x.y.z</version.quarkus>`
2. Edit the `pom.xml` with the new **Quarkus Platform version** which will induce the Quarkus version for generated applications: `<quarkus-platform-version>x.y.z</quarkus-platform-version>`
3. Provide a PR, merge, and [promote to production](#promote-to-production)


# API Documentation

- [OpenApi](http://editor.swagger.io/?url=https://code.quarkus.io/openapi)


