import { TextInput, TextInputProps } from '@patternfly/react-core';
import React, { useEffect, useState } from 'react';
import { useDebounce } from './use-debounce';

export interface DebouncedTextInputProps extends TextInputProps {
  delay?: number;
}

export function DebouncedTextInput(props: DebouncedTextInputProps) {
  const { value, onChange, ...rest } = props;
  const [localValue, setLocalValue] = useState(value);
  const [prevValue, setPrevValue] = useState<string | undefined>(undefined);
  const onChangeWithPrev = onChange ? (newVal: string, e: React.ChangeEvent<any>) => {
    setPrevValue(newVal);
    onChange(newVal, e);
  } : undefined;
  const debouncedOnChange = useDebounce(onChangeWithPrev, props.delay || 200);
  const onChangeWithLocal = debouncedOnChange ? (newVal: string, e: React.ChangeEvent<any>) => {
    setLocalValue(newVal);
    debouncedOnChange(newVal, e);
  }: undefined;
  useEffect(() => {
    if (value !== prevValue) {
      setLocalValue(value);
    }
    // eslint-disable-next-line
  }, [value]);
  return (
    <TextInput
      {...rest as any}
      onChange={onChangeWithLocal}
      value={onChange ? localValue: value}
    />
  );
}
