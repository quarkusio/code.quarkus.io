import React from 'react';
import './header.scss';

export function Header() {
  return (
    <div className="header">
      <div className="header-content responsive-container">
        <div className="brand">
          <a href="http://www.quarkus.io"><div className="brand-image" /></a>
        </div>
        <ul className="nav">
          <li><a href="https://quarkus.io/get-started/">Get Started</a></li>
          <li><a href="https://quarkus.io/guides/">Guides</a></li>
          <li><a className="active" href="https://code.quarkus.io/">Code</a></li>
          <li><a href="https://quarkus.io/community/">Community</a></li>
          <li><a href="https://quarkus.io/blog/">Blog</a></li>
        </ul>
      </div>
    </div>
  );
}