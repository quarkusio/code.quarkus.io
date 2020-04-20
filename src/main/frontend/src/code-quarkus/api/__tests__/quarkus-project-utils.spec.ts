import { cleanup } from '@testing-library/react';
import { parseProjectInQuery } from '../quarkus-project-utils';
import { Extension } from '../model';

afterEach(() => {
  cleanup();
});

const entries: Extension[] = [
  {
    'id': 'io.quarkus:quarkus-arc',
    'name': 'ArC',
    'version': 'test-version',
    'shortId': 'a',
    'tags': [],
    'keywords': [
      'arc',
      'cdi',
      'dependency-injection',
      'di',
      'label'
    ],
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
    'tags': ['preview'],
    'shortId': 'b',
    'default': false,
    'keywords': [
      'camel-netty4-http',
      'camel'
    ],
    'description': 'Camel support for Netty',
    'category': 'Integration',
    'order': 2,
  },
  {
    'id': 'some-id',
    'shortId': 'c',
    'version': 'test-version',
    'name': 'A CDI in name test',
    'tags': ['experimental'],
    'default': false,
    'keywords': [
      'lambda',
      'amazon-lambda',
      'aws-lambda',
      'amazon',
      'aws',
      'label'
    ],
    'shortName': 'a shortname',
    'description': 'Some description',
    'category': 'Cloud',
    'order': 1,
  },
  {
    'id': 'some-id',
    'shortId': 'c',
    'version': 'test-version',
    'name': 'A CDI in name test',
    'tags': ['experimental'],
    'default': false,
    'keywords': [
      'lambda',
      'amazon-lambda',
      'aws-lambda',
      'amazon',
      'aws',
      'label'
    ],
    'shortName': 'a shortname',
    'description': 'Some description',
    'category': 'Toto',
    'order': 1,
  },
];

describe('quarkus-project', () => {
  it('parseProjectInQuery correctly', () => {
    const parsedProject = parseProjectInQuery(entries, 'g=org.test&a=code-test&v=1.0.0-SNAPSHOT&b=MAVEN&c=org.toto&s=a.bG&cn=localhost');
    expect(parsedProject).toMatchSnapshot('no-github');
  });

  it('parseProjectInQuery correctly with github', () => {
    const parsedProject = parseProjectInQuery(entries, 'g=org.test&a=code-test&v=1.0.0-SNAPSHOT&b=GRADLE&c=org.toto&s=a.bG&cn=localhost&code=totototo&state=djdjdj&github=true');
    expect(parsedProject).toMatchSnapshot('github');
  });

  it('parseProjectInQuery with some other metadata', () => {
    const parsedProject = parseProjectInQuery(entries, 'v=1.0');
    expect(parsedProject).toMatchSnapshot('v=1.0');
  });

});