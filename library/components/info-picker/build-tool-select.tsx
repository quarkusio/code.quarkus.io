import React, { ChangeEvent } from 'react';
import { useAnalyticsEditionField } from '@quarkusio/code-quarkus.core.analytics';
import { InputProps } from '@quarkusio/code-quarkus.core.types';

export const BuildToolSelect = (props: InputProps<string>) => {
  const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
  const adaptedOnChange = (e: ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.currentTarget.value);
  };
  return (
    <div className="form-group">
      <label className="form-group-label" htmlFor="buildtool" arial-label="Choose build tool"><span
        className="form-group-label-text">Build Tool</span></label>
      <select id="buildtool" value={props.value} onChange={adaptedOnChange} className={'form-group-control'}>
        <option value={'MAVEN'}>Maven</option>
        <option value={'GRADLE'}>Gradle (Preview)</option>
        <option value={'GRADLE_KOTLIN_DSL'}>Gradle with Kotlin DSL (Preview)</option>
      </select>
    </div>
  );
};