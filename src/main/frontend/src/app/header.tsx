import React from 'react';
import './header.scss';

export function Header() {
  return (
    <div className="header">
      <div className="header-content">
        <div className="brand">
          <div className="brand-image" />
        </div>
        <ul className="nav">
          <li><a href="https://quarkus.io/get-started/">Get Started</a></li>
          <li><a href="https://quarkus.io/guides/">Guides</a></li>
          <li><a className="active" href="https://start.quarkus.io/">Project Generator</a></li>
          <li><a href="https://quarkus.io/community/">Community</a></li>
          <li><a href="https://quarkus.io/blog/">Blog</a></li>
        </ul>
      </div>
    </div>
  );
}