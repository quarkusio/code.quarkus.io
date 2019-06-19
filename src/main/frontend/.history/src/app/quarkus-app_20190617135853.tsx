import React from 'react';
import { LauncherDepsProvider } from '@launcher/component';
import { QuarkusFlow } from './quarkus-flow';

import '@patternfly/react-core/dist/styles/base.css';
import './quarkus-app.scss';

export function QuarkusApp() {
  return (
    <LauncherDepsProvider>
      <QuarkusFlow />
    </LauncherDepsProvider>
  );
}
