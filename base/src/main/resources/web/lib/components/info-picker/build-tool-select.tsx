import * as React from 'react';
import { useAnalyticsEditionField } from '../../core/analytics';
import { InputProps } from '../../core/types';
import { FaAngleDown } from 'react-icons/fa';

export const BuildToolSelect = (props: InputProps<string>) => {
  const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
  const adaptedOnChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.currentTarget.value);
  };
  return (
    <div className="form-group form-group-select">
      <label className="form-group-label" htmlFor="buildtool" arial-label="Choose build tool"><span
        className="form-group-label-text">Build Tool</span></label>
      <select id="buildtool" value={props.value} aria-label="Edit build tool" onChange={adaptedOnChange} className={'form-group-control form-group-select'}>
        <option value={'MAVEN'}>Maven</option>
        <option value={'GRADLE'}>Gradle</option>
        <option value={'GRADLE_KOTLIN_DSL'}>Gradle with Kotlin DSL</option>
      </select>
      <FaAngleDown />
    </div>
  );
};