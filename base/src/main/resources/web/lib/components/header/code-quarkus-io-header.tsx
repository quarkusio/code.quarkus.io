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
        <a href="https://code.camel.redhat.com/" onClick={linkTracker}><FaRedhat />Code with the Red Hat Build of Apache Camel</a>
        <a href="https://code.quarkus.ibm.com" onClick={linkTracker}><img className="icon-ibm" src="https://www.ibm.com/content/dam/adobe-cms/default-images/favicon.svg" alt="IBM" onError={(e) => { e.currentTarget.style.display = 'none'; (e.currentTarget.nextElementSibling as HTMLElement).style.display = 'inline-flex'; }} /><span className="icon-text-ibm" style={{display: 'none'}}>IBM</span>Code with the IBM Enterprise Build of Quarkus</a>
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