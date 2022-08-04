import * as React from 'react';
import { CSSProperties } from 'react';
import './stuff.scss';
import { CopyToClipboard } from './copy-to-clipboard';
import { Alert, Button } from 'react-bootstrap';
import { FaCog, FaExternalLinkAlt } from 'react-icons/fa';


export function optionalBool(val: (boolean | undefined), defaultValue: boolean): boolean {
  return val === undefined ? defaultValue : val!;
}

export function Code(props: { id: string, content: string, event: string[]  }) {
  return (
    <code className="code">
      <span className="content">{props.content}</span>
      <CopyToClipboard id={props.id} zIndex={5000} tooltipPlacement="left" event={props.event} content={`${props.content}`}/>
    </code>
  );
}

export function Spin(props: { children: React.ReactNode }) {
  return (
    <span className="animate-spin">
      {props.children}
    </span>
  );
}

export function Loader(props: { 'aria-label'?: string; error?: any; }) {
  return (
    <div className={'loader-or-error'} aria-label={props['aria-label']}>
      {!props || (!props!.error && (<Spin><FaCog /></Spin>))}
      {props && props.error &&
      <AlertError error={props.error}/>
      }
    </div>
  );
}

export function AlertError(props: { error: any }) {
  return (
    <Alert variant="danger" title="Something weird happened:" aria-label="error-while-loading-data" style={{ margin: '40px' }}>
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

export function Plural(props: {
  count: number;
  label: string | {plural: string; singular: string;};
}) {
  let isPlural = props.count === 0 || props.count > 1;
  if (typeof props.label === 'string') {
    return  <>{props.label}{isPlural ? 's' : ''}</>;
  }
  if (typeof props.label === 'object') {
    return <>{isPlural ? props.label.plural : props.label.singular}</>;
  }
  throw new Error("Invalid type");
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
      as="a"
      variant="link"
      href={props.href}
      aria-label={props['aria-label']}
      target={'_blank'}
      onClick={props.onClick}
    >
      {props.children} <FaExternalLinkAlt />
    </Button>);
}
