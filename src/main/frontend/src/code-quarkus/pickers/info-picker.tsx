import React, {ChangeEvent, useEffect} from 'react';
import {ExtendedTextInput, InputPropsWithValidation, optionalBool, TogglePanel} from '../../core';
import './info-picker.scss';
import {FormGroup} from "@patternfly/react-core";

export interface InfoPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
  packageName?: string;
  buildTool?: string;
}

interface InfoPickerProps extends InputPropsWithValidation<InfoPickerValue> {
  showMoreOptions?: boolean;
}

const ARTIFACTID_PATTERN = /^[a-z][a-z0-9-._]*$/;
const GROUPID_PATTERN = /^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$/;

const isValidId = (value?: string) => !!value && ARTIFACTID_PATTERN.test(value || '');
const isValidGroupId = (value?: string) => !!value && GROUPID_PATTERN.test(value);
const isValidInfo = (value: InfoPickerValue) => {
  return isValidGroupId(value.groupId)
    && isValidId(value.artifactId)
    && !!value.version
    && (!value.packageName || isValidGroupId(value.packageName))
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
  const onBuildToolChange = (event: ChangeEvent<HTMLSelectElement>) => onInputChange({ ...value, buildTool: event.target.value });

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
          pattern={GROUPID_PATTERN.source}
          isValid={isValidGroupId(value.groupId)}
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
          pattern={ARTIFACTID_PATTERN.source}
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
              pattern={GROUPID_PATTERN.source}
              isValid={isValidGroupId(value.packageName || value.groupId)}
            />
            <FormGroup
                fieldId="buildTool"
                label="Build Tool"
                aria-label="Choose build tool">
              <select id="buildtool" value={value.buildTool} onChange={onBuildToolChange} className={'pf-c-form-control'}>
                <option value={"MAVEN"}>Maven</option>
                <option value={"GRADLE"}>Gradle</option>
              </select>
            </FormGroup>
          </div>
        </TogglePanel>
      )}
    </div>
  );
};
