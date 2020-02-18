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

## How popular an extension is in the Code Quarkus app?

The category gives an overall overview of the popularity of an extension, then you can deep dive to particular actions related to extension 

| Category | Action | Label | Description |
| --- | --- | --- | --- |
| Extension | Used | *Extension ID* | This extension has been used in a project |
| Extension | Copy the command to add it with Maven | *Extension ID* | ... |
| Extension | Copy the command to add it with Gradle | *Extension ID* | ... |
| Extension | Copy GAV | *Extension ID* | ... |
| Extension | Click on "Open Extension Guide" link | *Extension ID* | ... |
| Extension | Display in search top 5 results | *Extension ID* | ... |

## What's the preferred way to get the application? / What is the content of generated applications?			

Some custom dimensions are available for those events:
- Extensions: sorted list of selected extension separated by comma
- Build Tool: the selected build tool
- Extension Quantity: the quantity of selected extensions
- Quarkus Version: The Quarkus version

| Category | Action | Label | Description |
| --- | --- | --- | --- |
| App | Download | *clientName* | A application has been generated for the specified client |

## How do users interact with code quarkus?

| Category | Action | Label | Description |
| --- | --- | --- | --- |
| UX | Generate application | Click on "Generate your application" button | ... |
| | Edit field | *Field name* | ... |
| | Post-Generate Popup Action | Start new | ... |
| | | Close | ... |
| | | Go back | ... |
| | | Copy "Dev mode" command | ... |
| | | Click "Download the zip" link | ... |
| | | Click "guides" link | ... |
| | Extension - Unselect | Keyboard/Selection/List | ... |
| | Extension - Select | Keyboard/Selection/List | ... |
| | Extension - Search | *Value* | ... |
| | Blurb | Click on "Missing a feature?" link | ... |
| | | Click on "Found a bug?" link | ... |
| | | Close | ... |