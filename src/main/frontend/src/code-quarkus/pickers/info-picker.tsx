import { FormGroup, Tooltip } from '@patternfly/react-core';
import React, { ChangeEvent } from 'react';
import { ExtendedTextInput, InputProps, optionalBool, TogglePanel, useAnalyticsEditionField } from '../../core';
import './info-picker.scss';

export interface InfoPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
  noExamples?: boolean;
  buildTool?: string;
}

interface InfoPickerProps extends InputProps<InfoPickerValue> {
  showMoreOptions?: boolean;
  quarkusVersion: string;
}

const ARTIFACTID_PATTERN = /^[a-z][a-z0-9-._]*$/;
const GROUPID_PATTERN = /^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$/;

const isValidId = (value?: string) => !!value && ARTIFACTID_PATTERN.test(value || '');
const isValidGroupId = (value?: string) => !!value && GROUPID_PATTERN.test(value);
export const isValidInfo = (value: InfoPickerValue) => {
  return isValidGroupId(value.groupId)
    && isValidId(value.artifactId)
    && !!value.version;
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
      <select id="buildtool" value={props.value} onChange={adaptedOnChange} className={'pf-c-form-control'}>
        <option value={'MAVEN'}>Maven</option>
        <option value={'GRADLE'}>Gradle (Preview)</option>
        <option value={'GRADLE_KOTLIN_DSL'}>Gradle with Kotlin DSL (Preview)</option>
      </select>
    </FormGroup>
  );
};

const ExamplesCheckbox = (props: InputProps<boolean>) => {
  const onChangeWithDirty = useAnalyticsEditionField('no-examples', props.onChange)[1];
  const adaptedOnChange = (e: ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.target.value === 'true', e);
  };
  return (
    <Tooltip
      position="right"
      content={<span>The flag <span className="codestart-example-icon" /> means the extension helps you get started with example code. You can choose to include all the examples or have an empty project....</span>}
      exitDelay={0}
      zIndex={200}
    >
      <FormGroup
        fieldId="no-examples"
        label={<span><span className="codestart-example-icon" />Example code</span>}
        aria-label="Examples">
        <select id="no-examples" value={props.value ? 'true' : 'false'} onChange={adaptedOnChange} className={'pf-c-form-control'}>
          <option value={'false'}>Yes, Please</option>
          <option value={'true'}>No, Thanks</option>
        </select>
      </FormGroup>
    </Tooltip>
  );
};

export const InfoPicker = (props: InfoPickerProps) => {

  const onInputChange = props.onChange;

  const onGroupIdChange = (newValue: string) => onInputChange({ ...props.value, groupId: newValue });
  const onArtifactIdChange = (newValue: string) => onInputChange({ ...props.value, artifactId: newValue });
  const onVersionChange = (newValue: string) => onInputChange({ ...props.value, version: newValue });
  const onNoExampleChange = (newValue: boolean) => onInputChange({ ...props.value, noExamples: newValue });
  const onBuildToolChange = (newValue: string) => onInputChange({ ...props.value, buildTool: newValue });

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
          value={props.value.groupId || ''}
          autoComplete="off"
          onChange={onGroupIdChange}
          pattern={GROUPID_PATTERN.source}
          isValid={isValidGroupId(props.value.groupId)}
        />
        <ExtendedTextInput
          label="Artifact"
          isRequired
          type="text"
          id="artifactId"
          name="artifactId"
          aria-label="Edit artifactId"
          value={props.value.artifactId || ''}
          autoComplete="off"
          onChange={onArtifactIdChange}
          pattern={ARTIFACTID_PATTERN.source}
          isValid={isValidId(props.value.artifactId)}
        />
        <SelectBuildTool onChange={onBuildToolChange} value={props.value.buildTool || 'MAVEN'}/>
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
              value={props.value.version || ''}
              autoComplete="off"
              onChange={onVersionChange}
              isValid={!!props.value.version}
            />
            <ExamplesCheckbox onChange={onNoExampleChange} value={props.value.noExamples || false} />
          </div>
        </TogglePanel>
      )}
    </div>
  );
};
