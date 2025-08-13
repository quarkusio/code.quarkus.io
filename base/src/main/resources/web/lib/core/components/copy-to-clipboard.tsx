import React, { MouseEvent, useEffect, useState } from 'react';
import copy from 'copy-to-clipboard';
import { useAnalytics } from '../../core/analytics'
import { OverlayTrigger, Popover } from 'react-bootstrap';
import { Placement } from 'react-bootstrap/Overlay';
import { FaClipboard, FaClipboardCheck, FaInfo } from 'react-icons/fa';
import './copy-to-clipboard.scss';
import classNames from 'classnames';

export interface CopyToClipboardProps {
  id: string;
  eventContext: object;
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
    if (props.eventContext && !copied) {
      analytics.event('Copy to Clipboard', props.eventContext);
    }
    setCopied(true);
  };

  const tooltip = props.light ? (
    <Popover id={props.id} className="copy-to-clipboard-popover" style={{ zIndex:props.zIndex || 100 }}>
      <Popover.Title as="p" className="indication">{copied ? <FaClipboardCheck/> : <FaClipboard/>}{copied ? 'It\'s in your clipboard!' : 'Copy this snippet to the clipboard'}</Popover.Title>
    </Popover>
  ) : (
    <Popover id={props.id} className="copy-to-clipboard-popover with-content" style={{ zIndex:props.zIndex || 100 }}>
      <Popover.Title as="p" className="indication"><FaInfo />{copied ? 'It\'s in your clipboard!' : 'Click to copy the snippet below to the clipboard:'}</Popover.Title>
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
    <OverlayTrigger  placement={props.tooltipPlacement} overlay={tooltip} delay={{ show: 0, hide: 0 }}>
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
