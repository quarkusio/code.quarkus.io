import React from 'react';
import { processExtensionsValues, search } from '../extensions-utils';
import { ExtensionEntry } from '../extensions-picker';

const entries: ExtensionEntry[] = [
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
      'label',
      'test'
    ],
    'default': false,
    'description': 'Build time CDI dependency injection',
    'category': 'Core',
    'platform': true,
    'order': 0,
  },
  {
    'id': 'io.quarkus:quarkus-camel-netty4-http-bab-ejfn-eafjna-fejanfj',
    'version': 'test-version',
    'name': 'Camel Netty4 test HTTP foo',
    'tags': [ 'status:preview', 'status:foo', 'some:test' ],
    'default': false,
    'keywords': [
      'quarkus-camel-netty4-http-bab-ejfn-eafjna-fejanfj',
      'cdi',
      'test',
      'camel-netty4-http',
      'camel',
    ],
    'platform': true,
    'description': 'Camel support for Netty',
    'category': 'Integration',
    'order': 2,
  },
  {
    'id': 'io.bar:some-id-foo-bar',
    'version': 'test-version',
    'name': 'A CDI bob test',
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
    'platform': false,
    'shortName': 'a shortname',
    'description': 'Some description',
    'category': 'Cloud',
    'order': 1,
  },
  {
    'id': 'io.ttt:arti',
    'version': 'test-version',
    'name': 'A Web cdi test 2',
    'tags': [ 'with:starter-code' ],
    'default': false,
    'keywords': [
      'lambda',
      'amazon-lambda',
      'aws-lambda',
      'amazon',
      'aws',
    ],
    'platform': true,
    'shortName': 'cdi',
    'description': 'Some description',
    'category': 'Web',
    'order': 3,
  }
];

const processedEntries = processExtensionsValues(entries);

describe('search', () => {
  it('"cdi bob in name" filters by name containing cdi and bob',
    () => expect(search('cdi bob in name', processedEntries)).toEqual([ entries[2] ]));
  it('"foo in name,artifact-id" filters by name or artifact-id containing foo',
    () => expect(search('foo in name,artifact-id', processedEntries)).toEqual([ entries[1], entries[2] ]));
  it('"foo bar in:name,artifact-id" filters by name or artifact-id containing foo and bar',
    () => expect(search('foo bar in name,artifact-id', processedEntries)).toEqual([ entries[2] ]));
  it('"name:"A CDI bob test"" filters by name equals "A CDI bob test"',
    () => expect(search('name:"A CDI bob test"', processedEntries)).toEqual([ entries[2] ]));
  it('"cat:cloud" filters by category equals "cloud"',
    () => expect(search('cat:cloud', processedEntries)).toEqual([ entries[2] ]));
  it('"cat:cloud,integration" filters by category equals "cloud" or "integration"',
    () => expect(search('cat:cloud,integration', processedEntries)).toEqual([ entries[1], entries[2] ]));
  it('"status:experimental" filters by status contains "experimental"',
    () => expect(search('status:experimental', processedEntries)).toEqual([ entries[2] ]));
  it('"status:experimental,preview" filters by status contains "experimental" or "preview',
    () => expect(search('status:experimental,preview', processedEntries)).toEqual([ entries[1], entries[2] ]));
  it('"cdi in name; status:experimental,preview" filters by status contains "experimental" or "preview and cdi in name',
    () => expect(search('cdi in name; status:experimental,preview', processedEntries)).toEqual([ entries[2] ]));
  it('"cdi in name status:experimental,preview" filters by status contains "experimental" or "preview and cdi in name',
    () => expect(search('cdi in name status:experimental,preview', processedEntries)).toEqual([ entries[2] ]));
  it('"cdi" returns the extension with cdi as shortName first',
    () => expect(search('cdi', processedEntries)).toEqual([ entries[3], entries[0], entries[1], entries[2] ]));
  it('"cdi test" is like "cdi test in name,shortname,keywords,category"',
    () => expect(search('cdi test', processedEntries)).toEqual(entries));
  it('"quarkus-camel-netty4-http-bab-ejfn-eafjna-fejanfj" works with default search"',
    () => expect(search('quarkus-camel-netty4-http-bab-ejfn-eafjna-fejanfj', processedEntries)).toEqual([ entries[1] ]));
  it('"quarkus-camel-netty4-http-bab-ejfn-eafjna-fejanfj in artifact" works with default search"',
    () => expect(search('quarkus-camel-netty4-http-bab-ejfn-eafjna-fejanfj', processedEntries)).toEqual([ entries[1] ]));
});
