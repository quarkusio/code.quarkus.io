import React from "react";
import { render, fireEvent, cleanup } from "@testing-library/react";
import { ExtensionsPicker, filterFunction, ExtensionEntry, getShortNames, sortFunction } from '../extensions-picker';

afterEach(() => {
  cleanup();
});

const entries: ExtensionEntry[] = [
  {
    "id": "io.quarkus:quarkus-arc",
    "name": "ArC",
    "labels": new Set([
      "arc",
      "cdi",
      "dependency-injection",
      "di",
      "label"
    ]),
    "description": "Build time CDI dependency injection",
    "shortName": "CDI",
    "category": "Core"
  },
  {
    "id": "io.quarkus:quarkus-camel-netty4-http",
    "name": "Camel Netty4 test HTTP",
    "labels": new Set([
      "camel-netty4-http",
      "camel"
    ]),
    "description": "Camel support for Netty",
    "category": "Integration"
  },
  {
    "id": "some-id",
    "name": "A CDI in name test",
    "labels": new Set([
      "lambda",
      "amazon-lambda",
      "aws-lambda",
      "amazon",
      "aws",
      "label"
    ]),
    "shortName": "a shortname",
    "description": "Some description",
    "category": "Cloud"
  },
];

const shortNames = getShortNames(entries);

describe('<ExtensionsPicker />', () => {

  it('renders the ExtensionsPicker correctly', () => {
    const comp = render(<ExtensionsPicker.Element placeholder="" entries={entries} value={{}} onChange={() => { }} />);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show results for valid search', async () => {
    const comp = render(<ExtensionsPicker.Element placeholder="" entries={entries} value={{}} onChange={() => { }} />);

    const searchField = comp.getByLabelText('Search extensions');
    fireEvent.change(searchField, { target: { value: 'CDI' } });
    const result = await comp.findAllByText(entries[0].description!);
    expect((result as HTMLElement[]).length).toBe(1);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('select values and save', async () => {
    const handleChange = jest.fn();
    const comp = render(<ExtensionsPicker.Element placeholder="" entries={entries} value={{}} onChange={handleChange} />);

    const searchField = comp.getByLabelText('Search extensions');
    fireEvent.change(searchField, { target: { value: 'netty' } });
    const item = await comp.findAllByText(entries[1].description!);
    fireEvent.click(item[0]);
    expect(comp.asFragment()).toMatchSnapshot();
  });
});

describe('getShortNames', () => {
  it('should work correctly', () => {
    expect(shortNames).toEqual(new Set(['cdi', 'a shortname']));
  });
})

describe('filterFunction', () => {
  it('when not using a filter, all extensions should be returned', () => {
    expect(entries.filter(filterFunction('', shortNames)))
      .toEqual(entries);
  });

  it('when using exact shortname, only the corresponding extension should be returned', () => {
    expect(entries.filter(filterFunction('cdi', shortNames)))
      .toEqual([entries[0]]);
  });

  it('when using start of shortname, the result should include the other corresponding extensions too', () => {
    expect(entries.filter(filterFunction('cd', shortNames)))
      .toEqual([entries[0], entries[2]]);
  });

  it('when using part of name, it should return the corresponding extensions', () => {
    expect(entries.filter(filterFunction('test', shortNames)))
      .toEqual([entries[1], entries[2]]);
  });

  it('when using label, it should return the corresponding extensions', () => {
    expect(entries.filter(filterFunction('label', shortNames)))
      .toEqual([entries[0], entries[2]]);
  });

  it('when using part of category (not start), it should not return it', () => {
    expect(entries.filter(filterFunction('oud', shortNames)))
      .toEqual([]);
  });

  it('when using start of category, it should return it', () => {
    expect(entries.filter(filterFunction('clou', shortNames)))
      .toEqual([entries[2]]);
  });

  it('when using start of category, it should return it', () => {
    expect(entries.filter(filterFunction('clou', shortNames)))
      .toEqual([entries[2]]);
  });
});

describe('sortFunction', () => {
  it('when using start of shortname of an extension, it should be first', () => {
    expect(sortFunction('cdi')(entries[0], entries[2]))
      .toEqual(-1);
    expect(sortFunction('cdi')(entries[2], entries[0]))
      .toEqual(1);
  });

  it('when using one of the label of an extension, it should be first', () => {
    expect(sortFunction('amazon')(entries[2], entries[0]))
      .toEqual(-1);
    expect(sortFunction('amazon')(entries[0], entries[2]))
      .toEqual(1);
  });

  it('when using start of the name of an extension, it should be first', () => {
    expect(sortFunction('a')(entries[0], entries[1]))
      .toEqual(-1);
    expect(sortFunction('a')(entries[1], entries[0]))
      .toEqual(1);
  });

  it('when no match, it should compare the names', () => {
    expect(sortFunction('nomatch')(entries[0], entries[1]))
      .toEqual(-1);
    expect(sortFunction('nomatch')(entries[1], entries[0]))
      .toEqual(1);
  });

});