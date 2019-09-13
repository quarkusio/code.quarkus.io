import React, { useState } from "react";
import copy from 'copy-to-clipboard';
import {ClipboardCheckIcon, ClipboardIcon} from "@patternfly/react-icons";
import { useAnalytics } from '../core';

export function CopyToClipboard(props: { eventId?: string, content: string }) {
  const [active, setActive] = useState(false);
  const [copied, setCopied] = useState(false);

  const analytics = useAnalytics();

  const copyToClipboard = () => {
    copy(props.content);
    if(props.eventId && !copied) {
      analytics && analytics.event('Copy-To-Clipboard', props.eventId, props.content);
    }
    setCopied(true);
    setTimeout(() => setCopied(false), 1000);
  }
  return (
    <div
      onMouseEnter={() => setActive(true)}
      onMouseLeave={() => setActive(false)}
      onClick={copyToClipboard}
      className="copy-to-clipboard"
      style={{cursor: 'pointer'}}
    >
      {active || copied ? <ClipboardCheckIcon /> : <ClipboardIcon />}
    </div>
  )

}
