import 'react-app-polyfill/ie11';
import 'core-js/es/number';
import '@patternfly/react-core/dist/styles/base.css';
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import * as serviceWorker from './serviceWorker';
import { SentryBoundary } from './sentry-boundary';
import { CodeQuarkus } from './code-quarkus/code-quarkus';
import { ConfigLoader } from './code-quarkus/config-loader';

ReactDOM.render(
  <ConfigLoader>{config =>
    <SentryBoundary sentryDSN={config.sentryDSN} environment={config.environment}>
      <CodeQuarkus config={config} />
    </SentryBoundary>
  }</ConfigLoader>
  , document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();
