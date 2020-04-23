import React, { useState } from 'react';
import './header.scss';
import { AngleLeftIcon, HandsHelpingIcon, RedhatIcon } from '@patternfly/react-icons';
import { createLinkTracker, useAnalytics } from '../../core/analytics';
import classNames from 'classnames';
import { Button } from '@patternfly/react-core';

function SupportButton(prop: {}) {
  const [opened, open] = useState(false);
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics,'UX', 'Support panel');
  const openPanel = (e: any) => {
    analytics.event('UX', 'Support panel', 'open');
    open(true);
  };
  const closePanel = (e: any) => {
    analytics.event('UX', 'Support panel', 'close');
    open(false);
  };
  return (
    <div className={classNames({ opened }, 'enterprise-support')} onMouseEnter={openPanel} onMouseLeave={closePanel}>
      <Button onClick={openPanel} aria-label="enterprise support"><HandsHelpingIcon/> Available with Enterprise Support</Button>
        <div className="support-panel">
          <a href="https://code.quarkus.redhat.com" onClick={linkTracker}><RedhatIcon />Code with the Red Hat Build of Quarkus</a>
        </div>
    </div>
  );
}

export function Header(props: { supportButton: boolean }) {
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics,'UX', 'Header');
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="quarkus-brand">
          <a href="/" onClick={linkTracker}>
            <img src="/static/media/quarkus-logo.svg" className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
        </div>
        <div className="nav-container">
          <a href="https://quarkus.io" onClick={linkTracker}><AngleLeftIcon/> Back to quarkus.io</a>
          {props.supportButton && (
            <SupportButton/>
          )}
        </div>
      </div>
    </div>
  );
}