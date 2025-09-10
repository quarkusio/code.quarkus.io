import * as React  from 'react';
import { optionalBool, ExtendedTextInput, TogglePanel  } from '../../core/components';
import { InputProps } from '../../core/types';
import './info-picker.scss';
import { BuildToolSelect } from './build-tool-select';
import { NoCodeSelect } from './no-code-select';
import { JavaVersionSelect } from './java-version-select';
import { Stream } from '../api/model';
import useMinWidth from "../../core/components/use-min-width";

export interface InfoPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
  noCode?: boolean;
  buildTool?: string;
  javaVersion?: string;
}

interface InfoPickerProps extends InputProps<InfoPickerValue> {
  showMoreOptions?: boolean;
  currentStream: Stream;
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

export const InfoPicker = (props: InfoPickerProps) => {
  const tooSmallForMore = useMinWidth(1200);
  const onInputChange = props.onChange;

  const onGroupIdChange = (newValue: string) => onInputChange({ ...props.value, groupId: newValue });
  const onArtifactIdChange = (newValue: string) => onInputChange({ ...props.value, artifactId: newValue });
  const onVersionChange = (newValue: string) => onInputChange({ ...props.value, version: newValue });
  const onNoCodeChange = (newValue: boolean) => onInputChange({ ...props.value, noCode: newValue });
  const onBuildToolChange = (newValue: string) => onInputChange({ ...props.value, buildTool: newValue });
  const onJavaVersionChange = (newValue: string) => onInputChange({ ...props.value, javaVersion: newValue });
  const showMoreOptions = !tooSmallForMore && props.showMoreOptions;
  return (
    <div className={'info-picker horizontal'}>
      <div className="base-settings form">
        <ExtendedTextInput
          label="Group"
          id="groupId"
          aria-label="Edit groupId"
          value={props.value.groupId || ''}
          onChange={onGroupIdChange}
          isValid={isValidGroupId(props.value.groupId)}
        />
        <ExtendedTextInput
          label="Artifact"
          id="artifactId"
          aria-label="Edit artifactId"
          value={props.value.artifactId || ''}
          onChange={onArtifactIdChange}
          isValid={isValidId(props.value.artifactId)}
        />
        <BuildToolSelect onChange={onBuildToolChange} value={props.value.buildTool || 'MAVEN'}/>
      </div>
      {optionalBool(showMoreOptions, true) && (
        <TogglePanel id="info-extended" mode="display" direction="horizontal" openLabel="More options" event="Extends app info" eventContext={{ location: 'info-picker' }}>
          <div className="extended-settings form">
            <ExtendedTextInput
              label="Version"
              id="projectVersion"
              aria-label="Edit project version"
              value={props.value.version || ''}
              onChange={onVersionChange}
              isValid={!!props.value.version}
            />
            <JavaVersionSelect javaCompatibility={props.currentStream?.javaCompatibility} onChange={onJavaVersionChange} value={props.value.javaVersion} />
            <NoCodeSelect onChange={onNoCodeChange} value={props.value.noCode || false} />
          </div>
        </TogglePanel>
      )}
    </div>
  );
};
