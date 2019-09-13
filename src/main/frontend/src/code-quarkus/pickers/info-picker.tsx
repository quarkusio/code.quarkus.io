import React, { useEffect } from 'react';
import { ExtendedTextInput, InputPropsWithValidation, optionalBool, TogglePanel } from '../../core';
import './info-picker.scss';

export interface InfoPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
  packageName?: string;
}

interface InfoPickerProps extends InputPropsWithValidation<InfoPickerValue> {
  showMoreOptions?: boolean;
}

const ID_REGEXP = /^[a-z][a-z0-9-.]{3,63}$/;
const PACKAGE_NAME_REGEXP = /^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$/;

const isValidId = (value?: string) => !!value && ID_REGEXP.test(value || '');
const isValidPackageName = (value?: string) => !!value && PACKAGE_NAME_REGEXP.test(value);
const isValidInfo = (value: InfoPickerValue) => {
  return isValidPackageName(value.groupId)
    && isValidId(value.artifactId)
    && !!value.version
    && isValidPackageName(value.packageName || value.groupId)
}

export const InfoPicker = (props: InfoPickerProps) => {
  const { value, isValid, onChange } = props;
  const onInputChange = (value: InfoPickerValue) => {
    props.onChange(value, isValidInfo(value));
  };

  useEffect(() => {
    if (isValid !== isValidInfo(value)) {
      onChange(value, !isValid);
    }
  }, [value, isValid, onChange])

  const onGroupIdChange = (newValue: string) => onInputChange({ ...value, groupId: newValue });
  const onArtifactIdChange = (newValue: string) => onInputChange({ ...value, artifactId: newValue });
  const onVersionChange = (newValue: string) => onInputChange({ ...value, version: newValue });
  const onPackageNameChange = (newValue: string) => onInputChange({ ...value, packageName: newValue });
  return (
    <div className={`info-picker horizontal`}>
      <div className="base-settings pf-c-form">
        <ExtendedTextInput
          label="Group"
          isRequired
          type="text"
          id="groupId"
          name="groupId"
          aria-label="Edit groupId"
          value={value.groupId || ''}
          autoComplete="off"
          onChange={onGroupIdChange}
          pattern={PACKAGE_NAME_REGEXP.source}
          isValid={isValidPackageName(value.groupId)}
        />
        <ExtendedTextInput
          label="Artifact"
          isRequired
          type="text"
          id="artifactId"
          name="artifactId"
          aria-label="Edit artifactId"
          value={value.artifactId || ''}
          autoComplete="off"
          onChange={onArtifactIdChange}
          pattern={ID_REGEXP.source}
          isValid={isValidId(value.artifactId)}
        />
      </div>
      {optionalBool(props.showMoreOptions, true) && (
        <TogglePanel id="info-extended" mode="horizontal" openLabel="Configure more options">
          <div className="extended-settings pf-c-form">
            <ExtendedTextInput
              label="Version"
              isRequired
              type="text"
              id="version"
              name="version"
              aria-label="Edit version"
              value={value.version || ''}
              autoComplete="off"
              onChange={onVersionChange}
              isValid={!!value.version}
            />
            <ExtendedTextInput
              label="Package Name"
              isRequired
              type="text"
              id="packageName"
              name="packageName"
              aria-label="Edit package name"
              value={value.packageName || value.groupId || ''}
              autoComplete="off"
              onChange={onPackageNameChange}
              pattern={PACKAGE_NAME_REGEXP.source}
              isValid={isValidPackageName(value.packageName || value.groupId)}
            />
          </div>
        </TogglePanel>
      )}
    </div>
  );
};
