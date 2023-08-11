import * as React from 'react';
import { useAnalyticsEditionField } from '@quarkusio/code-quarkus.core.analytics';
import { InputProps } from '@quarkusio/code-quarkus.core.types';
import { FaAngleDown } from 'react-icons/fa';

export const JavaVersionSelect = (props: InputProps<string>) => {
  const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
  const adaptedOnChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.currentTarget.value);
  };
  return (
    <div className="form-group form-group-select javaversion-select">
      <label className="form-group-label" htmlFor="javaversion" arial-label="Choose Java Version"><span
        className="form-group-label-text">Java Version</span></label>
      <select id="javaversion" value={props.value} onChange={adaptedOnChange} className={'form-group-control form-group-select'}>
        <option value={'11'}>11</option>
        <option value={'17'}>17</option>
      </select>
      <FaAngleDown />
    </div>
  );
};