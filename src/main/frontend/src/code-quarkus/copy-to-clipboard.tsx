import React, { useState, MouseEvent } from "react";
import copy from 'copy-to-clipboard';
import { ClipboardCheckIcon, ClipboardIcon } from "@patternfly/react-icons";
import { useAnalytics } from '../core';
import { Tooltip } from '@patternfly/react-core';

type TooltipPosition = 'auto' | 'top' | 'bottom' | 'left' | 'right';

export function CopyToClipboard(props: { eventId?: string, content: string, tooltipPosition?: TooltipPosition, zIndex?: number }) {
  const [active, setActive] = useState(false);
  const [copied, setCopied] = useState(false);
  const [copiedText, setCopiedText] = useState(false);

  const analytics = useAnalytics();

  const copyToClipboard = (e: MouseEvent) => {
    e.stopPropagation();
    copy(props.content);
    if (props.eventId && !copied) {
      analytics && analytics.event('Copy-To-Clipboard', props.eventId, props.content);
    }
    setCopied(true);
    setCopiedText(true);
    setTimeout(() => setCopiedText(false), 2000);
    setTimeout(() => setCopied(false), 1500);
  }
  const tooltip = copiedText ? <h3>Successfuly copied to clipboard!</h3> : <span>Copy to clipboard: <br /><code>{props.content}</code></span>;
  return (
    <Tooltip position={props.tooltipPosition} maxWidth="650px" content={tooltip} entryDelay={0} exitDelay={0} trigger="manual" isVisible={copied || active} zIndex={props.zIndex || 100}>
      <div
        onMouseEnter={() => setActive(true)}
        onMouseLeave={() => setActive(false)}
        onClick={copyToClipboard}
        className="copy-to-clipboard"
        style={{ cursor: 'pointer' }}
      >
        {active || copied ? <ClipboardCheckIcon /> : <ClipboardIcon />}
      </div>
    </Tooltip>
  )

}
