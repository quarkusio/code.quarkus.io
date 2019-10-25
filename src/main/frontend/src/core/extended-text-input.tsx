import React, { useState, FormEvent } from 'react';
import { TextInput, FormGroup, TextInputProps } from '@patternfly/react-core';
import { useAnalytics } from './analytics';

export interface ExtendedTextInputProps extends TextInputProps {
  id: string;
  helperTextInvalid?: string;
}

export function ExtendedTextInput(props: ExtendedTextInputProps) {
  const [isDirty, setIsDirty] = useState(false);
  const { onChange, isValid, helperTextInvalid, label, ...rest } = props;
  const analytics = useAnalytics();
  const valid = (!isDirty && !props.value) || isValid;
  const onChangeWithDirty = (value: string, event: FormEvent<HTMLInputElement>) => {
    if (!isDirty) {
      analytics.event('Input', 'Customized-Value', props.id);
    }
    setIsDirty(true);
    if (onChange) {
      onChange(value, event);
    }
  };
  return (
    <FormGroup
      fieldId={props.id}
      label={label}
      isValid={!helperTextInvalid ? undefined : valid}
      helperTextInvalid={helperTextInvalid}
    >
      <TextInput
        onChange={onChangeWithDirty}
        isValid={valid}
        {...rest as any}
      />
    </FormGroup>
  );
}
