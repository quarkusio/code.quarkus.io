import { cleanup, fireEvent, render } from '@testing-library/react';
import React from 'react';
import { ExtensionEntry, ExtensionsPicker } from '../extensions-picker';
import { act } from 'react-dom/test-utils';
import { DEFAULT_TAGS } from '../../api/code-quarkus-api';

afterEach(() => {
  cleanup();
});

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
];

describe('<ExtensionsPicker />', () => {

  it('renders the ExtensionsPicker correctly', () => {
    const setFilter = jest.fn();
    const comp = render(<ExtensionsPicker placeholder="" entries={entries} value={{ extensions: [] }} onChange={() => { }} buildTool="MAVEN" filter="" setFilter={setFilter} tagsDef={DEFAULT_TAGS} />);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show results for valid search', async () => {
    const setFilter = jest.fn();
    const comp = render(<ExtensionsPicker placeholder="" entries={entries} value={{ extensions: [] }} onChange={() => { }} buildTool="MAVEN" filter="" setFilter={setFilter} tagsDef={DEFAULT_TAGS} />);

    const searchField = comp.getByLabelText('Search extensions');
    fireEvent.change(searchField, { target: { value: 'CDI' } });
    expect(setFilter).toBeCalledTimes(1);
    expect(setFilter).lastCalledWith('CDI');
    act(() => {
      comp.rerender(<ExtensionsPicker placeholder="" entries={entries} value={{ extensions: [] }} onChange={() => { }} buildTool="MAVEN" filter="CDI" setFilter={setFilter} tagsDef={DEFAULT_TAGS} />);
    });
    const result = await comp.findAllByText(entries[0].description!);
    expect((result as HTMLElement[]).length).toBe(1);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('select values and save', async () => {
    const setFilter = jest.fn();
    const handleChange = jest.fn();
    const comp = render(<ExtensionsPicker placeholder="" entries={entries} value={{ extensions: [] }} onChange={handleChange} buildTool="MAVEN" filter="" setFilter={setFilter} tagsDef={DEFAULT_TAGS} />);

    const searchField = comp.getByLabelText('Search extensions');
    fireEvent.change(searchField, { target: { value: 'netty' } });
    expect(setFilter).toBeCalledTimes(1);
    expect(setFilter).lastCalledWith('netty');
    act(() => {
      comp.rerender(<ExtensionsPicker placeholder="" entries={entries} value={{ extensions: [] }} onChange={() => { }} buildTool="MAVEN" filter="netty" setFilter={setFilter} tagsDef={DEFAULT_TAGS} />);
    });
    const item = await comp.findAllByText(entries[1].description!);
    fireEvent.click(item[0]);
    expect(comp.asFragment()).toMatchSnapshot();
  });
});
