# Code Quarkus App

[![CircleCI](https://circleci.com/gh/fabric8-launcher/code.quarkus.io/tree/master.svg?style=svg)](https://circleci.com/gh/fabric8-launcher/code.quarkus.io/tree/master)

This is a standalone web-app to configure & generate a Quarkus project.

# Start the Code Quarkus App in dev mode

```
$ ./mvnw compile quarkus:dev
```


# Develop on the frontend

Open `./src/main/frontend` with your favorite IDE and use `yarn`:
```
$ yarn
$ yarn start
```

(in DEV mode, the frontend needs the backend to be started on http://localhost:8080)


# Update the extensions list

> This procedure will soon be easier, you can follow it here: https://github.com/fabric8-launcher/code.quarkus.io/issues/40

Provide PRs to udpate the Quarkus extensions list:
https://github.com/quarkusio/quarkus/blob/master/devtools/common/src/main/filtered/extensions.json
and
https://github.com/quarkusio/quarkusio.github.io/blob/develop/_data/extensions.yaml

Then

```
$ cd ./extensions
$ yarn
$ yarn start
```

It will automatically update the Code Quarkus App `extensions.json` list.

Then provide a PR to request a merge on master.
