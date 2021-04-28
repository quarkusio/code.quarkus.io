import { TextInputProps } from '@patternfly/react-core';
import React, { ChangeEvent, useState } from 'react';
import { useAnalytics } from './analytics';
import { DebouncedTextInput } from './debounced-text-input';

export interface ExtendedTextInputProps extends TextInputProps {
  id: string;
  helperTextInvalid?: string;
  isValid: boolean;
}

export function useAnalyticsEditionField(id: string, onChange: any): [boolean, (value: any, event: ChangeEvent<any>) => void] {
  const [isDirty, setIsDirty] = useState(false);
  const analytics = useAnalytics();
  const onChangeWithDirty = (value: string, event: ChangeEvent<any>) => {
    if (!isDirty) {
      analytics.event('UX', 'Edit field', id);
    }
    setIsDirty(true);
    if (onChange) {
      onChange(value, event);
    }
  };
  return [isDirty, onChangeWithDirty];
}

export function ExtendedTextInput(props: ExtendedTextInputProps) {
  const { value, onChange, isValid, helperTextInvalid, label, className, ...rest } = props;
  const [isDirty, onChangeWithDirty] = useAnalyticsEditionField(props.id, onChange);
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
      </div>

  );
}
