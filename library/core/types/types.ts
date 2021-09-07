import { FunctionComponent } from 'react';

export interface InputProps<T> {
  value: T;
  onChange: (value: T) => void;
}

export interface InputPropsWithValidation<T> {
  value: T;
  isValid: boolean;
  onChange: (value: T) => void;
}

export interface FormProps<T> {
  initialValue: T;
  onSave?(value: T): void;
  onCancel?(): void;
}

export interface Picker<P extends InputProps<V>, V> {
  Element: FunctionComponent<P>;
  checkCompletion(value: V): boolean;
}

export interface OverviewProps<T> {
  value: T;
  onClick: () => void;
}

export interface Hub<O = any> {
  id: string;
  title: string;
  Overview: FunctionComponent<O>;
}

export interface FormHub<V, P extends FormProps<V> = FormProps<V>, O extends OverviewProps<V> = OverviewProps<V>> extends Hub<O> {
  Form: FunctionComponent<P>;
  Overview: FunctionComponent<O>;
  checkCompletion(value: V): boolean;
}
