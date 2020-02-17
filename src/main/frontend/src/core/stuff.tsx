import { Alert, AlertVariant, Button, ButtonProps, Title } from '@patternfly/react-core';
import { InProgressIcon, ExternalLinkSquareAltIcon } from '@patternfly/react-icons';
import * as React from 'react';
import style from './stuff.module.scss';
import { CSSProperties } from 'react';


export function optionalBool(val: (boolean | undefined), defaultValue: boolean): boolean {
  return val === undefined ? defaultValue : val!;
}

export function Spin(props: { children: React.ReactNode }) {
  return (
    <span className={style.spin}>
      {props.children}
    </span>
  );
}

export function Loader(props: { 'aria-label'?: string; error?: any; }) {
  return (
    <div className={style.loader} aria-label={props['aria-label']}>
      {!props || (!props!.error && (<Spin><InProgressIcon/></Spin>))}
      {props && props.error &&
      <AlertError error={props.error}/>
      }
    </div>
  );
}

export function Separator() {
  return (
    <hr className={style.separator}/>
  );
}

export function ButtonLink(props: ButtonProps) {
  // @ts-ignore
  return (<Button component="a" {...props} />);
}

export function DescriptiveHeader(props: { title?: string, description: string }) {
  return (
    <div className={style.descriptiveHeader}>
      {props.title && (<Title size="lg">{props.title}</Title>)}
      <p>{props.description}</p>
    </div>
  );
}

export function SpecialValue(props: { children: string }) {
  return (
    <span className={style.specialValue}>{props.children}</span>
  );
}

export function AlertError(props: { error: any }) {
  return (
    <Alert variant={AlertVariant.danger} title="Something weird happened:" aria-label="error-in-hub-n-spoke" style={{margin: '40px'}}>
      {props.error.message || props.error.toString()}
    </Alert>
  );
}

export interface EffectSafety {
  callSafely: (fn: () => void) => void;
  unload: () => void;
}

export function effectSafety(): EffectSafety {
  const unMounted = { status: false };
  const unload = () => {
    unMounted.status = true;
  };
  const callSafely = (fn: () => void) => {
    if(!unMounted.status) {
      fn();
    }
  };
  return { callSafely, unload };
}

export function ExternalLink(props: {
  'aria-label'?: string;
  onClick?: React.MouseEventHandler<any>; 
  children: React.ReactNode;
  href: string;
  style?: CSSProperties;
}) {
  return (
    <Button
      style={props.style}
      component="a"
      variant="link"
      href={props.href}
      aria-label={props['aria-label']}
      target={'_blank'}
      onClick={props.onClick}
    >
        {props.children} <ExternalLinkSquareAltIcon />
    </Button>);
}
