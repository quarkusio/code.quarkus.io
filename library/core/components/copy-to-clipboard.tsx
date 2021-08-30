import React, { MouseEvent, useEffect, useState } from 'react';
import copy from 'copy-to-clipboard';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics'
import { OverlayTrigger, Popover } from 'react-bootstrap';
import { Placement } from 'react-bootstrap/Overlay';
import { FaClipboard, FaClipboardCheck } from 'react-icons/fa';
import './copy-to-clipboard.scss';
import classNames from 'classnames';

export interface CopyToClipboardProps {
  id: string;
  event: string[];
  content: string;
  children?: React.ReactNode;
  tooltipPlacement?: Placement;
  zIndex?: number;
  light?: boolean;
  className?: string;
  onClick?: (e: MouseEvent) => void;
}

export function CopyToClipboard(props: CopyToClipboardProps) {
  const [ copied, setCopied ] = useState(false);
  const [ active, setActive ] = useState(false);
  const analytics = useAnalytics();

  const copyToClipboard = (e: MouseEvent) => {
    e.stopPropagation();
    if (props.onClick) props.onClick(e);
    copy(props.content);
    if (props.event && props.event.length === 3 && !copied) {
      analytics.event(props.event[0], props.event[1], props.event[2]);
    }
    setCopied(true);
  };

  const tooltip = props.light ? (
    <Popover id={props.id} className="copy-to-clipboard-popover" style={{ zIndex:props.zIndex || 100 }}>
      <Popover.Title as="h5">{copied ? <FaClipboardCheck/> : <FaClipboard/>}{copied ? 'It\'s in your clipboard!' : 'Copy this snippet to clipboard'}</Popover.Title>
    </Popover>
  ) : (
    <Popover id={props.id} className="copy-to-clipboard-popover" style={{ zIndex:props.zIndex || 100 }}>
      <Popover.Title as="h3">{copied ? <FaClipboardCheck/> : <FaClipboard/>}{copied ? 'It\'s in your clipboard!' : 'Copy this snippet to clipboard'}</Popover.Title>
      <Popover.Content>
        <code><pre>{props.content}</pre></code>
      </Popover.Content>
    </Popover>
  )

  function onEnter() {
    return setActive(true);
  }

  useEffect(() => {
    const timer = setTimeout(() => {
      setCopied(false);
    }, 2000);
    return () => clearTimeout(timer);
  }, [ copied ]);

  function onLeave() {
    return setActive(false);
  }

  return (
    <OverlayTrigger  trigger="hover" placement={props.tooltipPlacement} overlay={tooltip} delay={{ show: 0, hide: 0 }}>
      <div
        onClick={copyToClipboard}
        className={classNames('copy-to-clipboard', props.className)}
        onMouseEnter={onEnter}
        onMouseLeave={onLeave}
        style={{ cursor: 'pointer' }}
      >
        {active || copied ? <FaClipboardCheck/> : <FaClipboard/>}
        <span className="copy-to-clipboard-label">{props.children}</span>
      </div>
    </OverlayTrigger>
  );
}
