import React from 'react';
import './code-quarkus-header.scss';
import { createLinkTracker, useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { StreamPicker, StreamPickerProps } from './stream-picker';
import defaultQuarkusLogo from '../media/quarkus-logo.svg';

export interface CodeQuarkusHeaderProps {
  streamProps: StreamPickerProps;
  quarkusLogo?: string;
}

export interface CompanyHeaderProps extends CodeQuarkusHeaderProps {
  children: JSX.Element;
}

export function CompanyHeader(props: CompanyHeaderProps) {
  const analytics = useAnalytics();
  const linkTracker = createLinkTracker(analytics, 'Header');
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="quarkus-brand">
          <a href="/" onClick={linkTracker}>
            <img src={props.quarkusLogo || defaultQuarkusLogo} className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
          <StreamPicker {...props.streamProps} />
        </div>
        <div className="nav-container">
          {props.children}
        </div>
      </div>
    </div>
  );
}