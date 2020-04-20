import React, { useState } from 'react';
import './header.scss';
import { AngleLeftIcon, HandsHelpingIcon, RedhatIcon } from '@patternfly/react-icons';
import { useAnalytics } from '../../core/analytics';
import classNames from 'classnames';
import { Button } from '@patternfly/react-core';

function SupportButton(prop: {}) {
  const [opened, open] = useState(false);
  const analytics = useAnalytics();

  const openPanel = (e: any) => {
    analytics.event('UX', 'Support panel', 'open');
    open(true);
  };
  const closePanel = (e: any) => {
    analytics.event('UX', 'Support panel', 'close');
    open(false);
  };
  const linkClick = (e: any) => {
    const link = e.target.getAttribute('href');
    analytics.event('UX', 'Click on support link', link);
  };
  return (
    <div className={classNames({ opened }, 'enterprise-support')} onMouseEnter={openPanel} onMouseLeave={closePanel}>
      <Button onClick={openPanel} aria-label="enterprise support"><HandsHelpingIcon/> Available with Enterprise Support</Button>
        <div className="support-panel">
          <a href="https://code.quarkus.redhat.com" onClick={linkClick}><RedhatIcon />Code with the Red Hat Build of Quarkus</a>
          <a href="https://support.quarkus.io" onClick={linkClick} className="secondary"><HandsHelpingIcon />Become a Quarkus Support Provider</a>
        </div>
    </div>
  );
}

export function Header(props: { supportButton: boolean }) {
  const analytics = useAnalytics();
  const linkClick = (e: any) => {
    const link = e.target.getAttribute('href');
    analytics.event('UX', 'Click on header link', link);
  };
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="quarkus-brand">
          <a href="/" onClick={linkClick}>
            <img src="/static/media/quarkus-logo.svg" className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
        </div>
        <div className="nav-container">
          <a href="https://quarkus.io" onClick={linkClick}><AngleLeftIcon/> Back to quarkus.io</a>
          {props.supportButton && (
            <SupportButton/>
          )}
        </div>
      </div>
    </div>
  );
}