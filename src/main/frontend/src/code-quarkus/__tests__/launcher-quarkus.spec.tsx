import { act, cleanup, fireEvent, render, RenderResult } from '@testing-library/react';
import * as React from 'react';
import { CodeQuarkus } from '../code-quarkus';

jest.mock('../backend-api', () => ({
  fetchExtensions: () => ([
    {
      "id": "io.quarkus:quarkus-arc",
      "shortId": "8mc",
      "name": "ArC",
      "keywords": [
        "arc",
        "cdi",
        "dependency-injection",
        "di"
      ],
      "description": "Build time CDI dependency injection",
      "shortName": "CDI",
      "category": "Core",
      "order": 2
    },
    {
      "id": "io.quarkus:quarkus-resteasy",
      "shortId": "ogy",
      "name": "RESTEasy JAX-RS",
      "keywords": [
        "resteasy",
        "jaxrs",
        "web",
        "rest"
      ],
      "description": "REST framework implementing JAX-RS and more",
      "shortName": "jax-rs",
      "category": "Web",
      "order": 4
    },
    {
      "id": "io.quarkus:quarkus-resteasy-jsonb",
      "shortId": "14pb",
      "name": "RESTEasy JSON-B",
      "keywords": [
        "resteasy-jsonb",
        "jaxrs-json",
        "resteasy-json",
        "resteasy",
        "jaxrs",
        "json",
        "jsonb"
      ],
      "description": "JSON-B serialization support for RESTEasy",
      "category": "Web",
      "order": 5
    },
    {
      "id": "io.quarkus:quarkus-resteasy-jackson",
      "shortId": "1aen",
      "name": "RESTEasy Jackson",
      "keywords": [
        "resteasy-jackson",
        "jaxrs-json",
        "resteasy-json",
        "resteasy",
        "jaxrs",
        "json",
        "jackson"
      ],
      "description": "Jackson serialization support for RESTEasy",
      "category": "Web",
      "order": 6
    },
  ]),
  fetchConfig: () => { throw new Error("not used"); }
}));


afterEach(() => {
  cleanup();
});

const config = { environment: 'test', quarkusVersion: 'test-version', gitCommitId: 'test-commitid' };

it('Render CodeQuarkus', async () => {
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus config={config} />);
    await comp.findByLabelText('Extensions picker');
  });
});

it('Let user Generate default application', async () => {
  window.open = jest.fn();
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus config={config} />);
    await comp.findByLabelText('Extensions picker');
  });

  // Generate
  const generateBtn = await comp!.findByLabelText('Generate your application');
  fireEvent.click(generateBtn);

  const downloadLink = await comp!.findByLabelText('Download link');
  expect(downloadLink.getAttribute('href')).toMatchSnapshot();
});

it('Let user customize an Application and Generate it', async () => {
  window.open = jest.fn();
  let comp: RenderResult;
  await act(async () => {
    comp = render(<CodeQuarkus config={config} />);
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
  const packageNameInput = await comp!.findByLabelText('Edit package name');
  fireEvent.change(packageNameInput, { target: { value: 'io.test.pack' } });

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

  const downloadLink = await comp!.findByLabelText('Download link');
  expect(downloadLink.getAttribute('href')).toMatchSnapshot();
});