import React, { useState } from 'react';
import './header.scss';
import { createLinkTracker, useAnalytics } from '../../core';
import classNames from 'classnames';
import { Button } from 'react-bootstrap';
import { FaAngleLeft, FaHandsHelping, FaRedhat } from 'react-icons/fa';
import { Platform, Stream } from '../api/model';

function SupportButton(prop: {}) {
  const [ opened, open ] = useState(false);
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
      <Button onClick={openPanel} aria-label="enterprise support"><FaHandsHelping/> Available with Enterprise Support</Button>
      <div className="support-panel">
        <a href="https://code.quarkus.redhat.com" onClick={linkTracker}><FaRedhat />Code with the Red Hat Build of Quarkus</a>
      </div>
    </div>
  );
}

const ERROR_STREAM: Stream = { key: 'recommended.not.found:stream', quarkusCoreVersion: 'error', recommended: true }

export function Header(props: { supportButton: boolean, platform: Platform }) {
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics,'UX', 'Header');
  const recommendedStream = props.platform.streams.find(s => s.recommended) || ERROR_STREAM;
  const recommendedStreamKeys = recommendedStream.key.split(':')
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="quarkus-brand">
          <a href="/" onClick={linkTracker}>
            <img src="/static/media/quarkus-logo.svg" className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
          <div className="current-quarkus-stream" title={`Quarkus core version: ${recommendedStream.quarkusCoreVersion}`}>
            <span className="platform-key">{recommendedStreamKeys[0]}</span>
            <span className="stream-id">{recommendedStreamKeys[1]}</span>
          </div>
        </div>
        <div className="nav-container">
          <a href="https://quarkus.io" onClick={linkTracker}><FaAngleLeft/> Back to quarkus.io</a>
          <SupportButton/>
        </div>
      </div>
    </div>
  );
}