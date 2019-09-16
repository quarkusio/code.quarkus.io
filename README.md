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

1. [Update the extension list](#update-the-code-quarkus-extension-list) with the new Quarkus version
2. Edit the `pom.xml` with the new Quarkus version `<version.quarkus>x.y.z</version.quarkus>`
3. Provide a PR, merge, and [promote to production](#promote-to-production)

# Update the Code Quarkus extension list

`x.y.z` is the Quarkus version.

```
$ yarn --cwd ./extensions && yarn --cwd ./extensions start x.y.z
```

It will automatically generate a new `extensions.json` to commit.

# Include a new extension

1. The extension must be available in a Quarkus release:
    https://github.com/quarkusio/quarkus/blob/x.y.z/devtools/common/src/main/filtered/extensions.json
2. The extension must be added to the website extension list (with metadata):
    https://github.com/quarkusio/quarkusio.github.io/blob/develop/_data/extensions.yaml
     > Updating the website will soon not be required anymore, you can follow progress here: https://github.com/quarkusio/code.quarkus.io/issues/40
3. [Update the extension list](#update-the-code-quarkus-extension-list)
   

# API Documentation

- [OpenApi](http://editor.swagger.io/?url=https://code.quarkus.io/openapi)


