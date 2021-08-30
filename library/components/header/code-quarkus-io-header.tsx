import React, { useState } from 'react';
import './code-quarkus-io-header.scss';
import { createLinkTracker, useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import classNames from 'classnames';
import { Button } from 'react-bootstrap';
import { FaAngleLeft, FaHandsHelping, FaRedhat } from 'react-icons/fa';
import { Platform, QuarkusProject, Stream } from '../api/model';
import { normalizeStreamKey } from '../api/quarkus-project-utils';
import logo from '../media/quarkus-logo.svg';

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

function getRecommendedStream(platform: Platform) {
  return platform.streams.find(s => s.recommended) || ERROR_STREAM;
}

function getProjectStream(platform: Platform, project: QuarkusProject) {
  if (!project.streamKey) {
    return null;
  }
  const recommendedStream = getRecommendedStream(platform);
  const normalizedStreamKey = normalizeStreamKey(recommendedStream.key.split(':')[0], project.streamKey);
  return platform.streams.find(s => s.key == normalizedStreamKey);
}

export function CodeQuarkusIoHeader(props: { platform: Platform, project: QuarkusProject, supportButton: boolean }) {
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics,'UX', 'Header');
  const recommendedStream = getRecommendedStream(props.platform);
  const stream = getProjectStream(props.platform, props.project) || recommendedStream;
  const streamKeys = stream.key.split(':');
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="quarkus-brand">
          <a href="/" onClick={linkTracker}>
            <img src={logo} className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
          <div className="current-quarkus-stream" title={`Quarkus core version: ${stream.quarkusCoreVersion}`}>
            <span className="platform-key">{streamKeys[0]}</span>
            <span className="stream-id">{streamKeys[1]}</span>
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