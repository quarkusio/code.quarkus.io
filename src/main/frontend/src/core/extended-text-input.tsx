import { FormGroup, TextInput, TextInputProps } from '@patternfly/react-core';
import React, { ChangeEvent, useState } from 'react';
import { useAnalytics } from './analytics';

export interface ExtendedTextInputProps extends TextInputProps {
  id: string;
  helperTextInvalid?: string;
}

export function useAnalyticsEditionField(id: string, onChange: any): [boolean, (value: string, event: ChangeEvent<any>) => void] {
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
  }
  return [isDirty, onChangeWithDirty];
}

export function ExtendedTextInput(props: ExtendedTextInputProps) {
  const { onChange, isValid, helperTextInvalid, label, className, ...rest } = props;
  const [isDirty, onChangeWithDirty] = useAnalyticsEditionField(props.id, onChange);
  const valid = (!isDirty && !props.value) || isValid;
  return (
    <FormGroup
      fieldId={props.id}
      label={label}
      isValid={!helperTextInvalid ? undefined : valid}
      helperTextInvalid={helperTextInvalid}
      className={className}
    >
      <TextInput
        onChange={onChangeWithDirty}
        isValid={valid}
        {...rest as any}
      />
    </FormGroup>
  );
}
