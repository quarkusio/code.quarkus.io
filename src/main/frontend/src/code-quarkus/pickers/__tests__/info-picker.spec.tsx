import React from 'react';
import {cleanup, fireEvent, render, RenderResult} from '@testing-library/react';
import {InfoPicker} from '../info-picker';
import {act} from 'react-dom/test-utils';

jest.useFakeTimers();

afterEach(cleanup);

describe('<InfoPicker />', () => {
  it('renders the InfoPicker correctly', () => {
    const onChange = jest.fn();
    const comp = render(<InfoPicker value={{ groupId: 'org.test', version: '1.0.0', artifactId: 'test' }} isValid={true} onChange={() => { }} quarkusVersion="1.0.0"/>);
    expect(comp.asFragment()).toMatchSnapshot();
    expect(onChange).toBeCalledTimes(0);
  });

  it('receive isValid after init when it has change', () => {
    const onChangeMock = jest.fn();
    const value = { groupId: 'org.test', version: '1.0.0', artifactId: 'test', packageName: 'org.package' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={value} isValid={false} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    act(() => { jest.runTimersToTime(210); });
    expect(onChangeMock).lastCalledWith(value, true);
  });

  it('receive isValid false when using invalid groupId', () => {
    const onChangeMock = jest.fn();
    const value = { groupId: 'org.test', version: '1.0.0', artifactId: 'test', packageName: 'org.package' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={value} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    fireEvent.change(comp!.getByLabelText('Edit groupId'), { target: { value: 'com.' } });
    act(() => { jest.runTimersToTime(210); });
    expect(onChangeMock).lastCalledWith({...value, groupId: 'com.'}, false);
  });

  it('receive isValid false when using invalid artifactId', () => {
    const onChangeMock = jest.fn();
    const value = { groupId: 'org.test', version: '1.0.0', artifactId: 'test', packageName: 'org.package' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={value} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    fireEvent.change(comp!.getByLabelText('Edit artifactId'), { target: { value: 'invalid id' } });
    act(() => { jest.runTimersToTime(210); });
    expect(onChangeMock).lastCalledWith({...value, artifactId: 'invalid id'}, false);
  });

  it('receive isValid false when using invalid project version', () => {
    const onChangeMock = jest.fn();
    const value = { groupId: 'org.test', version: '1.0.0', artifactId: 'test', packageName: 'org.package' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={value} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    fireEvent.change(comp!.getByLabelText('Edit package name'), { target: { value: 'com.1a' } });
    act(() => { jest.runTimersToTime(210); });
    expect(onChangeMock).lastCalledWith({...value, packageName: 'com.1a'}, false);
  });

  it('receive isValid false when using invalid package name', () => {
    const onChangeMock = jest.fn();
    const value = { groupId: 'org.test', version: '1.0.0', artifactId: 'test', packageName: 'org.package' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={value} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    fireEvent.change(comp!.getByLabelText('Edit package name'), { target: { value: 'com.1a' } });
    act(() => { jest.runTimersToTime(210); });
    expect(onChangeMock).lastCalledWith({...value, packageName: 'com.1a'}, false);
  });

  it('auto update untouched package name when groupId is edited', () => {
    const onChangeMock = jest.fn();
    const value = { groupId: 'org.test', version: '1.0.0', artifactId: 'test' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={value} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    fireEvent.change(comp!.getByLabelText('Edit groupId'), { target: { value: 'org.test.copy' } });
    act(() => {
      comp.rerender(<InfoPicker value={{...value, groupId: 'org.test.copy'}} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    expect(comp!.getByLabelText('Edit package name').getAttribute('value')).toBe('org.test.copy')

  });

  it('display errors when using invalid values', () => {
    const onChangeMock = jest.fn();
    const initialValue = { groupId: 'com.test', version: 'version', artifactId: 'test', packageName: 'org.package' };
    const invalidValue = { groupId: 'com.1t', version: '', artifactId: 'Te', packageName: 'org.package ' };
    let comp: RenderResult;
    act(() => {
      comp = render(<InfoPicker value={initialValue} isValid={true} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });
    fireEvent.change(comp!.getByLabelText('Edit groupId'), { target: { value: invalidValue.groupId } });
    fireEvent.change(comp!.getByLabelText('Edit artifactId'), { target: { value: invalidValue.artifactId } });
    fireEvent.change(comp!.getByLabelText('Edit project version'), { target: { value: invalidValue.version } });
    fireEvent.change(comp!.getByLabelText('Edit package name'), { target: { value: invalidValue.packageName } });

    act(() => { jest.runTimersToTime(210); });
    expect(onChangeMock).toHaveBeenCalledTimes(1);

    expect(comp!.getByLabelText('Edit groupId').getAttribute('aria-invalid')).toBe('true');
    expect(comp!.getByLabelText('Edit artifactId').getAttribute('aria-invalid')).toBe('true');
    expect(comp!.getByLabelText('Edit project version').getAttribute('aria-invalid')).toBe('true');
    expect(comp!.getByLabelText('Edit package name').getAttribute('aria-invalid')).toBe('true');
  });
});
