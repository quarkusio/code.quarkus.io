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

1. Edit the `pom.xml` with the new **Quarkus** & **compatible Quarkus Platform version**: 
```
    <!-- Quarkus version is used for bundling Code Quarkus -->
    <version.quarkus>x.y.z</version.quarkus>

    <!-- Quarkus Platform version must be compatible with Quarkus version -->
    <version.quarkus-platform>a.b.c</version.quarkus-platform>
```
2. Check that the `centos-quarkus-maven` image is matching the new Quarkus version: https://github.com/quarkusio/code.quarkus.io/blob/master/src/main/docker/Dockerfile.native.multistage#L2
3. Provide a PR, merge, and [promote to production](#promote-to-production)


# API Documentation

- [OpenApi](http://editor.swagger.io/?url=https://code.quarkus.io/openapi)

# Analytics events

| Category | Action | Label | Description |
| --- | --- | --- | --- |
| Picker | Add-Extension | *Extension ID* | The user selects an extension |
| Picker | Remove-Extension | *Extension ID* | The user unselects an extension |
| Picker | Search-Extension | *Not set* | The user has used the extension search at least once in one page view |
| Extension | Used | *Extension ID* | The user has generated an application with this extension |
| Extension | Combined | *Extension IDS* | The user has generated an application with those extensions (sorted & separated by a comma) |
| App | Generate | *Quarkus version* | The user has generated an application this Quarkus version |
| Input | Customized-Value | *Field Name* | The user has edited this field at least once in one page view |
| Copy-To-Clipboard | Add-Extension-Command | *Command* | The user has copied this command to add an extension to the clipboard (at the right of the extension list) |
| Copy-To-Clipboard | Start-Dev-Command | *Command* | The user has copied the command to start developing to the clipboard (in the after generation popup) |



