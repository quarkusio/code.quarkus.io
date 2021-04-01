# Code Quarkus App

# Backend dev

Start in watch mode:
```bash
make dev-backend
```

# Full stack dev

Start in watch mode:
```bash
make dev
```

Then open `./src/main/frontend` with your favorite IDE to edit:


# Staging

Staging is auto-updated with main (it takes 15min to refresh after a merge): https://stage.code.quarkus.io

You can check deployed commit hash on: https://stage.code.quarkus.io/api/config

# Promote to production (temporarily only available to Red Hat developers)

1. Check that everything works as expected on [staging](#staging)
2. (Inside Red Hat private network) Create a PR on this [link](https://gitlab.cee.redhat.com/service/app-interface/-/edit/main/data/services/quarkus/cicd/ci-ext/saas.yaml) with the commit hash to release in the `ref: ...` with the commit hash of to release
3. Comment with `/lgtm` and wait for CI checks
4. Merging the PR will trigger a deployment to production


# To update the Quarkus version (after a new Quarkus release)

1. Rebase the `next` branch from `main` to make sure it is up to date
2. Edit the `pom.xml` with the new **Quarkus** version & **Quarkus Platform BOM**: 
```
    <!-- Quarkus version is used for bundling Code Quarkus -->
    <version.quarkus>x.y.z</version.quarkus>

    <!-- Quarkus Platform version -->
    <quarkus.platform.group-id>io.quarkus</quarkus.platform.group-id>
    <quarkus.platform.artifact-id>quarkus-universe-bom</quarkus.platform.artifact-id>
    <quarkus.platform.version>a.b.c</quarkus.platform.version>
n.quarkus-platform>
```
3. Check that you are using the `quarkus-universe-bom` and not `quarkus-bom` as `quarkus.platform.artifact-id`
4. Check that the `centos-quarkus-maven` image is up to date: https://github.com/quarkusio/code.quarkus.io/blob/main/src/main/docker/Dockerfile.multistage#L2
5. Create a PR from your `next` to `main`
6. [Promote to production](#promote-to-production)

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

## Infra Managed by Red Hat app-sre team 

All those links have restricted access:

- Staging: https://console-openshift-console.apps.app-sre-stage-0.k3s7.p1.openshiftapps.com/k8s/cluster/projects
- Production: https://console-openshift-console.apps.app-sre-prod-01.i7w5.p1.openshiftapps.com/k8s/cluster/projects/code-quarkus-production
- CI/CD: https://ci.ext.devshift.net/view/quarkus/
- Jira: https://issues.redhat.com/projects/APPSRE/issues/
- Infra repository: https://gitlab.cee.redhat.com/service/app-interface
- Sentry: https://sentry.devshift.net/
- Slack: https://coreos.slack.com (channel sd-app-sre)

## Debug

Start latest quay image locally:
```bash
docker run -i --rm -p 8080:8080 quay.io/quarkus/code-quarkus-app:latest
```

To request access, please contact adamevin@redhat.com
