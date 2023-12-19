import * as React from 'react';
import { useAnalyticsEditionField } from '../../core/analytics';
import { InputProps } from '../../core/types';
import { FaAngleDown } from 'react-icons/fa';
import { JavaCompatibility } from '../api/model';

interface JavaVersionSelectProps extends InputProps<string> {
  javaCompatibility?: JavaCompatibility;
}

export const JavaVersionSelect = (props: JavaVersionSelectProps) => {
  const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
  const adaptedOnChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    onChangeWithDirty(e.currentTarget.value);
  };
  return (
    <div className="form-group form-group-select javaversion-select">
      <label className="form-group-label" htmlFor="javaversion" arial-label="Choose Java Version"><span
        className="form-group-label-text">Java Version</span></label>
      <select id="javaversion" aria-label="Edit Java version" value={props.value || props.javaCompatibility?.recommended || '17'} onChange={adaptedOnChange} className={'form-group-control form-group-select'}>
        {props.javaCompatibility?.versions.map((version) => <option key={version} value={version}>{version}</option>)}
      </select>
      <FaAngleDown />
    </div>
  );
};