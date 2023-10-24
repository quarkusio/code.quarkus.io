import { cleanup } from '@testing-library/react';
import { mapExtensions, parseProjectInQuery, toShortcut, retrieveProjectFromLocalStorage, LocalStorageKey, saveProjectToLocalStorage, resetProjectToDefault, existsStoredProject, parse } from '../quarkus-project-utils';
import { Extension, QuarkusProject } from '../model';

beforeAll(()=> {
  configureLocalstorageMock();
});

afterEach(() => {
  cleanup();
});

function configureLocalstorageMock() {
  jest.spyOn(window.localStorage.__proto__, "setItem");
  jest.spyOn(window.localStorage.__proto__, "getItem");
  jest.spyOn(window.localStorage.__proto__, "removeItem");

  window.localStorage.__proto__.setItem = jest.fn();
  window.localStorage.__proto__.getItem = jest.fn();
  window.localStorage.__proto__.removeItem = jest.fn();
}

const entries: Extension[] = [
  {
    'id': 'io.quarkus:quarkus-arc',
    'name': 'ArC',
    'version': 'test-version',
    'tags': [],
    'keywords': [
      'arc',
      'cdi',
      'dependency-injection',
      'di',
      'label'
    ],
    'platform': true,
    'default': false,
    'description': 'Build time CDI dependency injection',
    'shortName': 'CDI',
    'category': 'Core',
    'order': 0,
  },
  {
    'id': 'io.quarkus:quarkus-camel-netty4-http',
    'version': 'test-version',
    'name': 'Camel Netty4 test HTTP',
    'tags': [ 'status:preview' ],
    'default': false,
    'keywords': [
      'camel-netty4-http',
      'camel'
    ],
    'platform': true,
    'description': 'Camel support for Netty',
    'category': 'Integration',
    'order': 2,
  },
  {
    'id': 'some-id',
    'version': 'test-version',
    'name': 'A CDI in name test',
    'tags': [ 'status:experimental' ],
    'default': false,
    'keywords': [
      'lambda',
      'amazon-lambda',
      'aws-lambda',
      'amazon',
      'aws',
      'label'
    ],
    'platform': true,
    'shortName': 'a shortname',
    'description': 'Some description',
    'category': 'Cloud',
    'order': 1,
  },
  {
    'id': 'some-id3',
    'version': 'test-version',
    'name': 'A CDI in name test',
    'tags': [ 'status:experimental' ],
    'default': false,
    'keywords': [
      'lambda',
      'amazon-lambda',
      'aws-lambda',
      'amazon',
      'aws',
      'label'
    ],
    'platform': true,
    'shortName': 'a shortname',
    'description': 'Some description',
    'category': 'Toto',
    'order': 1,
  },
];

describe('quarkus-project', () => {
  it('parseProjectInQuery correctly', () => {
    const queryParams = parse('g=org.test&a=code-test&v=1.0.0-SNAPSHOT&b=MAVEN&c=org.toto&e=some-id&e=arc&e=quarkus-camel-netty4-http');
    const parsedProject = parseProjectInQuery(queryParams);
    expect(parsedProject).toMatchSnapshot('no-github');
  });

  it('parseProjectInQuery correctly with github', () => {
    const queryParams = parse('g=org.test&a=code-test&v=1.0.0-SNAPSHOT&b=GRADLE&c=org.toto&e=some-id&e=arc&e=quarkus-camel-netty4-http&code=totototo&state=djdjdj&github=true');
    const parsedProject = parseProjectInQuery(queryParams);
    expect(parsedProject).toMatchSnapshot('github');
  });

  it('parseProjectInQuery with some other metadata', () => {
    const parsedProject = parseProjectInQuery(parse('v=1.0'));
    expect(parsedProject).toMatchSnapshot('v=1.0');
  });

  it('map extensions', () => {
    const mappedExtensions = mapExtensions(entries, [ 'arc', 'quarkus-camel-netty4-http' ]);
    expect(mappedExtensions).toMatchSnapshot('mappedExtensions');
  });

  it('toShortcut should work properly', () => {
    expect(toShortcut('io.quarkus:quarkus-my-ext')).toBe('my-ext');
    expect(toShortcut('quarkus-my-ext')).toBe('my-ext');
    expect(toShortcut('my-quarkus-ext')).toBe('my-quarkus-ext');
    expect(toShortcut('io.quarkiverse.myext:quarkus-my-ext')).toBe('io.quarkiverse.myext:quarkus-my-ext');
    expect(toShortcut('org.apache.camel.quarkus:camel-quarkus-core')).toBe('org.apache.camel.quarkus:camel-quarkus-core');
  });

  it('retrieveProjectFromLocalStorage should load app config from localstorage', () => {
    retrieveProjectFromLocalStorage();

    expect(localStorage.getItem).toHaveBeenCalledWith(LocalStorageKey.DEFAULT_PROJECT);
  });

  it('saveProjectToLocalStorage should save app config to localstorage', () => {
    const quarkusProject = parseProjectInQuery(parse('g=org.test&a=code-test&e=arc')) as QuarkusProject;
    const jsonProject = JSON.stringify(quarkusProject);

    saveProjectToLocalStorage(quarkusProject);

    expect(localStorage.setItem).toHaveBeenCalledWith(LocalStorageKey.DEFAULT_PROJECT, jsonProject);
  });

  it('resetProjectToDefault delete app config to localstorage', () => {
    resetProjectToDefault();

    expect(localStorage.removeItem).toHaveBeenCalledWith(LocalStorageKey.DEFAULT_PROJECT);
  });


  it('existsStoredProject should search for app config on localstorage', () => {
    existsStoredProject();

    expect(localStorage.getItem).toHaveBeenCalledWith(LocalStorageKey.DEFAULT_PROJECT);
  });
  
});