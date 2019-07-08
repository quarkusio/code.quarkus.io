import React from "react";
import { render, fireEvent, cleanup } from "@testing-library/react";
import { ExtensionsPicker } from '../extensions-picker';

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

describe('<ExtensionsPicker />', () => {

  const entries = [
    {
      "id": "io.quarkus:quarkus-arc",
      "description": "Build time CDI dependency injection",
      "name": "ArC",
      "metadata": { "category": "Core" }
    },
    {
      "name": "Netty",
      "description": "Netty is a non-blocking I/O client-server framework. Used by Quarkus as foundation layer.",
      "id": "io.quarkus:quarkus-netty",
      "metadata": { "category": "Web" }
    },
  ]

  it('renders the ExtensionsPicker correctly', () => {
    const comp = render(<ExtensionsPicker.Element placeholder="" entries={entries} value={{}} onChange={() => { }} />);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show results for valid search', async () => {
    const comp = render(<ExtensionsPicker.Element placeholder="" entries={entries} value={{}} onChange={() => {}} />);

    const searchField = comp.getByLabelText('Search extensions');
    fireEvent.change(searchField, { target: { value: 'CDI' } });
    const result = await comp.findAllByText(entries[0].description);
    expect((result as HTMLElement[]).length).toBe(1);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('select values and save', async () => {
    const handleChange = jest.fn();
    const comp = render(<ExtensionsPicker.Element placeholder="" entries={entries} value={{}} onChange={handleChange} />);

    const searchField = comp.getByLabelText('Search extensions');
    fireEvent.change(searchField, { target: { value: 'Netty' } });
    const item = await comp.findAllByText(entries[1].description);
    fireEvent.click(item[0]);
    expect(comp.asFragment()).toMatchSnapshot();
  });

});
