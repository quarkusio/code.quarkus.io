import { FormGroup, Tooltip } from '@patternfly/react-core';
import React, { ChangeEvent, useEffect, useState } from 'react';
import { ExtendedTextInput, InputProps, InputPropsWithValidation, optionalBool, TogglePanel, useAnalyticsEditionField, useDebounce } from '../../core';
import './info-picker.scss';

export interface InfoPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
  packageName?: string;
  buildTool?: string;
}

interface InfoPickerProps extends InputPropsWithValidation<InfoPickerValue> {
  showMoreOptions?: boolean;
  quarkusVersion: string;
}

const ARTIFACTID_PATTERN = /^[a-z][a-z0-9-._]*$/;
const GROUPID_PATTERN = /^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$/;

const isValidId = (value?: string) => !!value && ARTIFACTID_PATTERN.test(value || '');
const isValidGroupId = (value?: string) => !!value && GROUPID_PATTERN.test(value);
const isValidInfo = (value: InfoPickerValue) => {
  return isValidGroupId(value.groupId)
    && isValidId(value.artifactId)
    && !!value.version
    && (!value.packageName || isValidGroupId(value.packageName));
};

const SelectBuildTool = (props: InputProps<string>) => {
  const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
  const adaptedOnChange = (e: ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.target.value, e);
  };
  return (
    <FormGroup
      fieldId="buildTool"
      label="Build Tool"
      aria-label="Choose build tool">
      <select id="buildtool" value={props.value || 'MAVEN'} onChange={adaptedOnChange} className={'pf-c-form-control'}>
        <option value={'MAVEN'}>Maven</option>
        <option value={'GRADLE'}>Gradle (Preview)</option>
      </select>
    </FormGroup>
  );
};

export const InfoPicker = (props: InfoPickerProps) => {
  const [localValue, setLocalValue] = useState<InfoPickerValue>(props.value);
  const onChange = useDebounce(props.onChange, 200, { leading: true, trailing: true });

  const onInputChange = (value: InfoPickerValue) => {
    setLocalValue(value);
    onChange(value, isValidInfo(value));
  };

  useEffect(() => {
    if (props.isValid !== isValidInfo(localValue)) {
      onChange(localValue, !props.isValid);
    }
    // eslint-disable-next-line
  }, []);

  const onGroupIdChange = (newValue: string) => onInputChange({ ...localValue, groupId: newValue });
  const onArtifactIdChange = (newValue: string) => onInputChange({ ...localValue, artifactId: newValue });
  const onVersionChange = (newValue: string) => onInputChange({ ...localValue, version: newValue });
  const onPackageNameChange = (newValue: string) => onInputChange({ ...localValue, packageName: newValue });
  const onBuildToolChange = (newValue: string) => onInputChange({ ...localValue, buildTool: newValue });
  const configFileName = localValue.buildTool === 'MAVEN' ? 'pom.xml' : 'gradle.properties';
  const packageName = localValue.packageName === undefined ? localValue.groupId : localValue.packageName;
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
          value={localValue.groupId || ''}
          autoComplete="off"
          onChange={onGroupIdChange}
          pattern={GROUPID_PATTERN.source}
          isValid={isValidGroupId(localValue.groupId)}
        />
        <ExtendedTextInput
          label="Artifact"
          isRequired
          type="text"
          id="artifactId"
          name="artifactId"
          aria-label="Edit artifactId"
          value={localValue.artifactId || ''}
          autoComplete="off"
          onChange={onArtifactIdChange}
          pattern={ARTIFACTID_PATTERN.source}
          isValid={isValidId(localValue.artifactId)}
        />
        <SelectBuildTool onChange={onBuildToolChange} value={localValue.buildTool || 'MAVEN'}/>
      </div>
      {optionalBool(props.showMoreOptions, true) && (
        <TogglePanel id="info-extended" mode="horizontal" openLabel="Configure more options" event={['UX', 'Application Info - Configure More Options']}>
          <div className="extended-settings pf-c-form">
            <ExtendedTextInput
              label="Version"
              isRequired
              type="text"
              id="projectVersion"
              name="projectVersion"
              aria-label="Edit project version"
              value={localValue.version || ''}
              autoComplete="off"
              onChange={onVersionChange}
              isValid={!!localValue.version}
            />
            <ExtendedTextInput
              label="Package Name"
              isRequired
              type="text"
              id="packageName"
              name="packageName"
              aria-label="Edit package name"
              value={packageName || ''}
              autoComplete="off"
              onChange={onPackageNameChange}
              pattern={GROUPID_PATTERN.source}
              isValid={isValidGroupId(packageName)}
            />

            <Tooltip
              position="right"
              content={`You may change the Quarkus Version after generation in the ${configFileName}. Just be cautious with extension compatibility.`}
              exitDelay={0}
              zIndex={200}
            >
              <ExtendedTextInput
                label="Quarkus Version"
                isRequired
                type="text"
                id="quarkusVersion"
                name="quarkusVersion"
                aria-label="Quarkus Version"
                value={props.quarkusVersion}
                isReadOnly={true}
                className="quarkus-version"
              />
            </Tooltip>
          </div>
        </TogglePanel>
      )}
    </div>
  );
};
