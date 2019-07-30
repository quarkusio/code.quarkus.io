# Quarkus Launcher

[![CircleCI](https://circleci.com/gh/fabric8-launcher/launcher-quarkus/tree/master.svg?style=svg)](https://circleci.com/gh/fabric8-launcher/launcher-quarkus/tree/master)

This is a standalone web-app to configure & generate a Quarkus project.

# Start the Quarkus Launcher in dev mode

```
$ ./mvnw compile quarkus:dev
```


# Develop on the frontend

Open `./src/main/frontend` with your favorite IDE and use `yarn`:
```
$ yarn
$ yarn start
```


# Update the extensions list

> This procedure will soon be easier, you can follow it here: https://github.com/fabric8-launcher/launcher-quarkus/issues/40

Provide PRs to udpate the Quarkus extensions list:
https://github.com/quarkusio/quarkus/blob/master/devtools/common/src/main/filtered/extensions.json
and
https://github.com/quarkusio/quarkusio.github.io/blob/develop/_data/extensions.yaml

Then

```
$ cd ./src/main/frontend/extensions
$ yarn
$ yarn start
```

It will automatically update the Quarkus Launcher `extensions.json` list.

Then provide a PR to request a merge on master.
