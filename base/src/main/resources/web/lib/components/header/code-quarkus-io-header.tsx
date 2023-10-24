import React, { useState } from 'react';
import './code-quarkus-io-header.scss';
import { createLinkTracker, useAnalytics } from '../../core/analytics';
import classNames from 'classnames';
import { Button } from 'react-bootstrap';
import { FaAngleLeft, FaHandsHelping, FaRedhat } from 'react-icons/fa';
import { CompanyHeader, CodeQuarkusHeaderProps } from './code-quarkus-header';

function SupportButton(prop: {}) {
  const [ opened, open ] = useState(false);
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics, 'Support panel');
  const openPanel = (e: any) => {
    analytics.event('Open Support panel', { element: 'support-button' });
    open(true);
  };
  const closePanel = (e: any) => {
    analytics.event('Close Support panel', { element: 'support-button' });
    open(false);
  };
  return (
    <div className={classNames({ opened }, 'enterprise-support')} onMouseEnter={openPanel} onMouseLeave={closePanel}>
      <Button onClick={openPanel} aria-label="enterprise support"><FaHandsHelping/> Available with Enterprise Support</Button>
      <div className="support-panel">
        <a href="https://code.quarkus.redhat.com" onClick={linkTracker}><FaRedhat />Code with the Red Hat Build of Quarkus</a>
      </div>
    </div>
  );
}

export function CodeQuarkusIoHeader(props: CodeQuarkusHeaderProps) {
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics, 'Header');
  return (
    <CompanyHeader {...props}>
      <>
        <a href="https://quarkus.io" onClick={linkTracker}><FaAngleLeft/> Back to quarkus.io</a>
        <SupportButton/>
      </>
    </CompanyHeader>
  );
}