import React from 'react';
import { cleanup, fireEvent, render, RenderResult } from '@testing-library/react';
import { InfoPicker, isValidInfo } from '../info-picker';
import { act } from 'react-dom/test-utils';
import { wait } from '@testing-library/dom';
import { newDefaultProject } from '../../api/quarkus-project-utils';

afterEach(cleanup);

describe('<InfoPicker />', () => {
  it('renders the InfoPicker correctly', () => {
    const onChange = jest.fn();
    const comp = render(<InfoPicker value={{ groupId: 'org.test', version: '1.0.0', artifactId: 'test' }} onChange={() => {
    }} quarkusVersion="1.0.0"/>);
    expect(comp.asFragment()).toMatchSnapshot();
    expect(onChange).toBeCalledTimes(0);
  });

  it('display errors when using invalid values', async () => {
    const onChangeMock = jest.fn();
    const initialValue = { groupId: 'com.test', version: 'version', artifactId: 'test', packageName: 'org.package' };
    const invalidValue = { groupId: 'com.1t', version: '', artifactId: 'Te', packageName: 'org.package ' };
    let comp: RenderResult;
    let value = initialValue;
    act(() => {
      comp = render(<InfoPicker value={value} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });


    fireEvent.change(comp!.getByLabelText('Edit groupId'), { target: { value: invalidValue.groupId } });
    value = { ...value, groupId: invalidValue.groupId };
    expect(onChangeMock).toHaveBeenCalledTimes(1);
    expect(onChangeMock).lastCalledWith(value);
    act(() => {
      comp.rerender(<InfoPicker value={value} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });

    fireEvent.change(comp!.getByLabelText('Edit artifactId'), { target: { value: invalidValue.artifactId } });
    value = { ...value, artifactId: invalidValue.artifactId };
    expect(onChangeMock).toHaveBeenCalledTimes(2);
    expect(onChangeMock).lastCalledWith(value);
    act(() => {
      comp.rerender(<InfoPicker value={value} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });

    fireEvent.change(comp!.getByLabelText('Edit project version'), { target: { value: invalidValue.version } });
    value = { ...value, version: invalidValue.version };
    expect(onChangeMock).toHaveBeenCalledTimes(3);
    expect(onChangeMock).lastCalledWith(value);
    act(() => {
      comp.rerender(<InfoPicker value={value} onChange={onChangeMock} quarkusVersion="1.0.0"/>);
    });

    expect(comp!.getByLabelText('Edit groupId').getAttribute('aria-invalid')).toBe('true');
    expect(comp!.getByLabelText('Edit artifactId').getAttribute('aria-invalid')).toBe('true');
    expect(comp!.getByLabelText('Edit project version').getAttribute('aria-invalid')).toBe('true');
  });

  it('isValidInfo is false when info is invalid', () => {
    const defaultMetadata = newDefaultProject().metadata;
    expect(isValidInfo({ ...defaultMetadata, groupId: 'com.1a' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, groupId: 'com.1a-' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, groupId: '1com.test' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, groupId: '' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, artifactId: '1com' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, artifactId: 'comA' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, artifactId: 'com$' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, artifactId: '' })).toBe(false);
    expect(isValidInfo({ ...defaultMetadata, version: '' })).toBe(false);
  });

  it('isValidInfo is true when info is valid', () => {
    const defaultMetadata = newDefaultProject().metadata;
    expect(isValidInfo({ ...defaultMetadata, groupId: 'com.test_toto' })).toBe(true);
    expect(isValidInfo({ ...defaultMetadata, artifactId: 'art-t_tt.t12' })).toBe(true);
    expect(isValidInfo({ ...defaultMetadata, version: 'test version' })).toBe(true);
  });


});
