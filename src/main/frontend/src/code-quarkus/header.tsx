import React from 'react';
import './header.scss';

export function Header() {
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="brand">
          <a href="/">
            <img src="quarkus_logo.svg" className="project-logo" title="Quarkus" alt="Quarkus"/>
          </a>
        </div>
        <div className="nav-container">
          <ul className="nav-list">
            <li><a href="https://quarkus.io/get-started/">Get Started</a></li>
            <li><a href="https://quarkus.io/guides/">Guides</a></li>
            <li><a href="https://quarkus.io/community/">Community</a></li>
            <li><a href="https://quarkus.io/blog/">Blog</a></li>
            <li><a className="active" href="/">Start Coding</a></li>
          </ul>
        </div>
      </div>
    </div>
  );
}