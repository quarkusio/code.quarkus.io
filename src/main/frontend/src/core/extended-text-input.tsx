import React, { useState } from 'react';
import { useAnalytics } from './analytics';
import { DebouncedTextInput, DebouncedTextInputProps } from './debounced-text-input';
import { FaExclamationTriangle } from 'react-icons/fa';

export interface ExtendedTextInputProps extends DebouncedTextInputProps {
  id: string;
  label: string;
}

export function useAnalyticsEditionField(id: string, onChange: any): [boolean, (event: any) => void] {
  const [ isDirty, setIsDirty ] = useState(false);
  const analytics = useAnalytics();
  const onChangeWithDirty = (event: any) => {
    if (!isDirty) {
      analytics.event('UX', 'Edit field', id);
    }
    setIsDirty(true);
    if (onChange) {
      onChange(event);
    }
  };
  return [ isDirty, onChangeWithDirty ];
}

export function ExtendedTextInput(props: ExtendedTextInputProps) {
  const { value, onChange, isValid, label, className, ...rest } = props;
  const [ isDirty, onChangeWithDirty ] = useAnalyticsEditionField(props.id, onChange);
  const valid = (!isDirty && !props.value) || isValid;
  return (
    <div className="form-group">
      <label className="form-group-label" htmlFor={props.id}><span
        className="form-group-label-text">{label}</span></label>
      <DebouncedTextInput
        className="form-group-control"
        {...rest as any}
        onChange={onChangeWithDirty}
        isValid={valid}
        value={value}
      />
      {!isValid && <FaExclamationTriangle className="is-invalid-icon" />}
    </div>
  );
}
