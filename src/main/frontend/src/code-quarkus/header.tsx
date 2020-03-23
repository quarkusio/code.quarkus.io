import React from 'react';
import './header.scss';
import {AngleLeftIcon} from "@patternfly/react-icons";
import {useAnalytics} from "../core/analytics";

export function Header() {
  const analytics = useAnalytics();
  const linkClick = (e: any) => {
    const link = e.target.getAttribute('href');
    analytics.event("UX", "Click on header link", link);
  };
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="brand">
          <a href="/" onClick={linkClick}>
            <img src="/static/media/quarkus-logo.svg" className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
        </div>
        <div className="nav-container">
          <a href="https://quarkus.io" onClick={linkClick}><AngleLeftIcon /> Back to quarkus.io</a>
        </div>
      </div>
    </div>
  );
}