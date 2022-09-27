import { act, cleanup, fireEvent, render, RenderResult } from '@testing-library/react';
import * as React from 'react';
import { CodeQuarkus } from '../code-quarkus';
import { Config, Platform, QuarkusProject } from '../api/model';
import { Api } from '../api/code-quarkus-api';
import { LocalStorageKey } from '../api/quarkus-project-utils';

const fetchPlatform = async (api: Api, streamKey?: string) => (
  {
    'streams': [
      {
        'key': 'io.quarkus.platform:2.0',
        'recommended': true,
        'quarkusCoreVersion': '2.0.0.Final',
        'status': 'FINAL'
      }
    ],
    'extensions': [
      {
        'id': 'io.quarkus:quarkus-arc',
        'version': 'test-version',
        'name': 'ArC',
        'keywords': [
          'arc',
          'cdi',
          'dependency-injection',
          'di'
        ],
        'platform': true,
        'description': 'Build time CDI dependency injection',
        'shortName': 'CDI',
        'category': 'Core',
        'order': 2
      },
      {
        'id': 'io.quarkus:quarkus-resteasy',
        'version': 'test-version',
        'name': 'RESTEasy JAX-RS',
        'keywords': [
          'resteasy',
          'jaxrs',
          'web',
          'rest'
        ],
        'platform': true,
        'description': 'REST framework implementing JAX-RS and more',
        'shortName': 'jax-rs',
        'category': 'Web',
        'order': 4
      },
      {
        'id': 'io.quarkus:quarkus-resteasy-jsonb',
        'version': 'test-version',
        'name': 'RESTEasy JSON-B',
        'keywords': [
          'resteasy-jsonb',
          'jaxrs-json',
          'resteasy-json',
          'resteasy',
          'jaxrs',
          'json',
          'jsonb'
        ],
        'platform': true,
        'description': 'JSON-B serialization support for RESTEasy',
        'category': 'Web',
        'order': 5
      },
      {
        'id': 'io.quarkus:quarkus-resteasy-jackson',
        'version': 'test-version',
        'name': 'RESTEasy Jackson',
        'keywords': [
          'resteasy-jackson',
          'jaxrs-json',
          'resteasy-json',
          'resteasy',
          'jaxrs',
          'json',
          'jackson'
        ],
        'platform': true,
        'description': 'Jackson serialization support for RESTEasy',
        'category': 'Web',
        'order': 6
      }
    ] } as Platform
);

const fetchConfig = async (api: Api) => ({
  environment: 'test', quarkusPlatformVersion: 'test-version', gitCommitId: 'test-commitid'
} as Config);

const api: Api = {
  backendUrl: 'localhost:8080', clientName: 'test', requestOptions: {}
}

const storedProject = {
  "metadata": {
      "groupId":"quarkus.test",
      "artifactId":"quarkus-test",
      "version":"1.0.0-SNAPSHOT",
      "buildTool":"MAVEN",
      "javaVersion":"11",
      "noCode":false
    },
  "extensions":[
    "io.quarkus:quarkus-resteasy",
    "io.quarkus:quarkus-resteasy-jsonb"
  ]};

afterEach(() => {
  cleanup();
});

it('Render CodeQuarkus', async () => {
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform}/>);
    await comp.findByLabelText('Extensions picker');
  });
});

it('Let user Generate default application', async () => {
  window.open = jest.fn();
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform}/>);
    await comp.findByLabelText('Extensions picker');
  });

  // Generate
  const generateBtn = await comp!.findByLabelText('Generate your application');
  fireEvent.click(generateBtn);

  const downloadLink = await comp!.findByLabelText('Download the zip');
  expect(downloadLink.getAttribute('href')).toMatchSnapshot();
});

it('Let user customize an Application and Generate it', async () => {
  window.open = jest.fn();
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform}/>);
    await comp.findByLabelText('Extensions picker');
  });

  // Custom GAV
  const groupIdInput = await comp!.findByLabelText('Edit groupId');
  fireEvent.change(groupIdInput, { target: { value: 'io.test.group' } });
  const artifactIdInput = await comp!.findByLabelText('Edit artifactId');
  fireEvent.change(artifactIdInput, { target: { value: 'custom-test-app' } });
  const toggleMoreOptionsBtn = await comp!.findByLabelText('Toggle panel');
  fireEvent.click(toggleMoreOptionsBtn);
  const versionInput = await comp!.findByLabelText('Edit project version');
  fireEvent.change(versionInput, { target: { value: '1.0.0-TEST' } });

  // Select extensions
  const ext1 = await comp!.findByLabelText('Switch io.quarkus:quarkus-arc extension');
  fireEvent.click(ext1);

  const ext2 = await comp!.findByLabelText('Switch io.quarkus:quarkus-resteasy extension');
  fireEvent.click(ext2);

  const ext3 = await comp!.findByLabelText('Switch io.quarkus:quarkus-resteasy-jackson extension');
  fireEvent.click(ext3);

  // Generate
  const generateBtn = await comp!.findByLabelText('Generate your application');
  fireEvent.click(generateBtn);

  const downloadLink = await comp!.findByLabelText('Download the zip');
  expect(downloadLink.getAttribute('href')).toMatchSnapshot();
});

it('Let user save app config', async () => {
  window.open = jest.fn();
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform}/>);
    await comp.findByLabelText('Extensions picker');
  });

  //Show options
  const toggleBtn = await comp!.container.getElementsByClassName("generate-button-split-more dropdown-toggle")[0];
  fireEvent.mouseOver(toggleBtn);

  expect(localStorage.getItem(LocalStorageKey.DEFAULT_PROJECT)).toBeNull();
  
  const saveBtn = await comp!.findByLabelText('Store current app as default');
  fireEvent.click(saveBtn);

  expect(localStorage.getItem(LocalStorageKey.DEFAULT_PROJECT)).not.toBeNull();
});


it('When a stored app config exists, it should be loaded.', async () => {
  //save app config to localstorage
  localStorage.setItem(LocalStorageKey.DEFAULT_PROJECT, JSON.stringify(storedProject));

  //open app
  window.open = jest.fn();
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform}/>);
    await comp.findByLabelText('Extensions picker');
  });

  const toggleMoreOptionsBtn = await comp!.findByLabelText('Toggle panel');
  fireEvent.click(toggleMoreOptionsBtn);

  //Project Metadata
  const groupIdInput = await comp!.findByLabelText('Edit groupId');
  const artifactIdInput = await comp!.findByLabelText('Edit artifactId');
  const versionInput = await comp!.findByLabelText('Edit project version');
  expect(groupIdInput.getAttribute("value")).toBe("quarkus.test");
  expect(artifactIdInput.getAttribute("value")).toBe("quarkus-test");
  expect(versionInput.getAttribute("value")).toBe("1.0.0-SNAPSHOT");

  // Selected extensions
  const selectedExtensions = await comp!.container.getElementsByClassName("extension-row selected");
  expect(selectedExtensions.length).toBe(2);

  const selectedExtToggle = await comp!.findByLabelText("Selected extensions");
  fireEvent.mouseOver(selectedExtToggle);
  const ext1 = await comp!.findByText("RESTEasy JAX-RS")
  const ext2 = await comp!.findByText("RESTEasy JSON-B")
  expect(ext1).not.toBeNull();
  expect(ext2).not.toBeNull();
});