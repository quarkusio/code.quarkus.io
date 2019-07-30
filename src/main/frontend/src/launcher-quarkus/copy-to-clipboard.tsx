import React, { useState } from "react";
import copy from 'copy-to-clipboard';
import {ClipboardCheckIcon, ClipboardIcon} from "@patternfly/react-icons";

export function CopyToClipboard(props: { content: string }) {
  const [active, setActive] = useState(false);
  const [copied, setCopied] = useState(false);

  const copyToClipboard = () => {
    copy(props.content);
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
