import '@patternfly/react-core/dist/styles/base.css';
import React from 'react';
import { Header } from './header';
import './quarkus-app.scss';
import { QuarkusFlow } from './quarkus-flow';

export function QuarkusApp() {
  return (
    <>
      <div className="quarkus-app">
        <Header />
        <QuarkusFlow />
      </div>
    </>
  );
}
