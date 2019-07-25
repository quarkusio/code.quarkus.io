import '@patternfly/react-core/dist/styles/base.css';
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import * as serviceWorker from './serviceWorker';
import { SentryBoundary } from './sentry-boundary';
import { LauncherQuarkus } from './launcher-quarkus/launcher-quarkus';
import { ConfigLoader } from './launcher-quarkus/config-loader';

ReactDOM.render(
  <ConfigLoader>{config =>
    <SentryBoundary sentryDSN={config.sentryDSN} environment={config.environment}>
      <LauncherQuarkus config={config} />
    </SentryBoundary>
  }</ConfigLoader>
  , document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();
