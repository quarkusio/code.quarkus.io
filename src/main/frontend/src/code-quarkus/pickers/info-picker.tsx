import React, { useEffect } from 'react';
import { ExtendedTextInput, optionalBool, TogglePanel, InputPropsWithValidation } from '@launcher/component';
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
    && isValidPackageName(value.packageName)
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
  return (
    <div className={`info-picker horizontal`}>
      <div className="base-settings pf-c-form">
        <ExtendedTextInput
          label="Group"
          isRequired
          helperTextInvalid="Please provide a valid groupId"
          type="text"
          id="groupId"
          name="groupId"
          aria-label="Edit groupId"
          value={value.groupId || ''}
          autoComplete="off"
          onChange={newValue => onInputChange({ ...value, groupId: newValue })}
          pattern={PACKAGE_NAME_REGEXP.source}
          isValid={isValidPackageName(value.groupId)}
        />
        <ExtendedTextInput
          label="Artifact"
          isRequired
          helperTextInvalid="Please provide a valid artifactId"
          type="text"
          id="artifactId"
          name="artifactId"
          aria-label="Edit artifactId"
          value={value.artifactId || ''}
          autoComplete="off"
          onChange={newValue => onInputChange({ ...value, artifactId: newValue })}
          pattern={ID_REGEXP.source}
          isValid={isValidId(value.artifactId)}
        />
      </div>
      {optionalBool(props.showMoreOptions, true) && (
        <TogglePanel id="info-extended" mode="horizontal" openLabel="Configure more options">
          <div className="extended-settings pf-c-form">
            <ExtendedTextInput
              label="Version"
              helperTextInvalid="Please provide a valid version"
              isRequired
              type="text"
              id="version"
              name="version"
              aria-label="Edit version"
              value={value.version || ''}
              autoComplete="off"
              onChange={newValue => onInputChange({ ...value, version: newValue })}
              isValid={!!value.version}
            />
            <ExtendedTextInput
              label="Package Name"
              helperTextInvalid="Please provide a package name"
              isRequired
              type="text"
              id="packageName"
              name="packageName"
              aria-label="Edit package name"
              value={value.packageName || ''}
              autoComplete="off"
              onChange={newValue => onInputChange({ ...value, packageName: newValue })}
              pattern={PACKAGE_NAME_REGEXP.source}
              isValid={isValidPackageName(value.packageName)}
            />
          </div>
        </TogglePanel>
      )}
    </div>
  );
};
