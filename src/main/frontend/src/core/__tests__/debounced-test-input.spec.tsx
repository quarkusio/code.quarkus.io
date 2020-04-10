import React from 'react';
import { cleanup, fireEvent, render } from '@testing-library/react';
import { DebouncedTextInput } from '../debounced-text-input';
import { act } from 'react-dom/test-utils';

jest.useFakeTimers();

afterEach(() => {
  cleanup();
});

describe('<DebouncedTextInput />', () => {
  it('renders the ExtendedTextInput correctly', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    expect(comp.asFragment()).toMatchSnapshot();
    expect(comp.getByLabelText('debounced-input').getAttribute('value')).toBe('toto');
    expect(onChange).toBeCalledTimes(0);
  });

  it('Call onChange without delay when called once', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value' } });
    expect(onChange).toBeCalledTimes(1);
    expect(onChange).lastCalledWith('some value', expect.anything());
  });

  it('Call onChange to be called after a delay for the second immediate change', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value' } });
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some new value' } });
    expect(onChange).toBeCalledTimes(1);
    expect(onChange).lastCalledWith('some value', expect.anything());
    act(() => { jest.advanceTimersByTime(200); });
    expect(onChange).toBeCalledTimes(2);
    expect(onChange).lastCalledWith('some new value', expect.anything());
  });

  it('Call onChange to be called after a delay for the second change', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value' } });
    expect(onChange).toBeCalledTimes(1);
    expect(onChange).lastCalledWith('some value', expect.anything());
    act(() => { jest.advanceTimersByTime(50); });
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some new value' } });
    expect(onChange).toBeCalledTimes(1);
    act(() => { jest.advanceTimersByTime(200); });
    expect(onChange).toBeCalledTimes(2);
    expect(onChange).lastCalledWith('some new value', expect.anything());
  });

  it('Call onChange only with the latest value', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value' } });
    act(() => { jest.advanceTimersByTime(50); });
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value 2' } });
    act(() => { jest.advanceTimersByTime(50); });
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value 3' } });
    expect(onChange).toBeCalledTimes(1);
    expect(onChange).lastCalledWith('some value', expect.anything());
    act(() => { jest.advanceTimersByTime(200); });
    expect(onChange).toBeCalledTimes(2);
    expect(onChange).lastCalledWith('some value 3', expect.anything());
  });

  it('Uses the provided value on rerender', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    act(() => {
      comp.rerender(<DebouncedTextInput value="new-value" onChange={onChange} aria-label="debounced-input"/>);
    });
    expect(comp.getByLabelText('debounced-input').getAttribute('value')).toBe('new-value');
  });

  it('Uses the provided value on rerender after a change', () => {
    const onChange = jest.fn();
    const comp = render(<DebouncedTextInput value="toto" onChange={onChange} aria-label="debounced-input"/>);
    fireEvent.change(comp.getByLabelText('debounced-input'), { target: { value: 'some value' } });
    act(() => {
      comp.rerender(<DebouncedTextInput value="new-value" onChange={onChange} aria-label="debounced-input"/>);
    });
    expect(comp.getByLabelText('debounced-input').getAttribute('value')).toBe('new-value');
  });

});
