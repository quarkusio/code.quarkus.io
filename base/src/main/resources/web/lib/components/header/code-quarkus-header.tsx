import React from 'react';
import './code-quarkus-header.scss';
import { createLinkTracker, useAnalytics } from '../../core/analytics';
import { StreamPicker, StreamPickerProps } from './stream-picker';
import defaultQuarkusLogo from '../media/quarkus-logo.svg';

export interface CodeQuarkusHeaderProps {
  streamProps: StreamPickerProps;
  quarkusLogo?: string;
  brand?: JSX.Element;
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
        <div className="nav-left">
          <div className="brand">
            {props.brand || (
              <a href="/" onClick={linkTracker} className="quarkus-brand">
                <img src={props.quarkusLogo || `${defaultQuarkusLogo}`} className="project-logo" title="Quarkus" alt="Quarkus"/>
              </a>
            )}
          </div>
          <StreamPicker {...props.streamProps} />
        </div>
        <div className="nav-right">
          {props.children}
        </div>
      </div>
    </div>
  );
}