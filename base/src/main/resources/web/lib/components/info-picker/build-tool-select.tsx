import * as React from 'react';
import { useAnalyticsEditionField } from '../../core/analytics';
import { InputProps } from '../../core/types';
import { FaAngleDown } from 'react-icons/fa';
import { BuildToolCompatibility } from '../api/model';

interface BuildToolSelectProps extends InputProps<string> {
  buildToolCompatibility?: BuildToolCompatibility;
}

const BUILD_TOOL_LABELS: { [key: string]: string } = {
  'MAVEN': 'Maven',
  'GRADLE': 'Gradle',
  'GRADLE_KOTLIN_DSL': 'Gradle with Kotlin DSL'
};

export const BuildToolSelect = (props: BuildToolSelectProps) => {
  const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
  const adaptedOnChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.currentTarget.value);
  };

  const availableTools = props.buildToolCompatibility?.tools || ['MAVEN', 'GRADLE', 'GRADLE_KOTLIN_DSL'];

  return (
    <div className="form-group form-group-select">
      <label className="form-group-label" htmlFor="buildtool" arial-label="Choose build tool"><span
        className="form-group-label-text">Build Tool</span></label>
      <select id="buildtool" value={props.value} aria-label="Edit build tool" onChange={adaptedOnChange} className={'form-group-control form-group-select'}>
        {availableTools.map(tool => (
          <option key={tool} value={tool}>{BUILD_TOOL_LABELS[tool]}</option>
        ))}
      </select>
      <FaAngleDown />
    </div>
  );
};
