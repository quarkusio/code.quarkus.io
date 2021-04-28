import 'react-app-polyfill/ie11';
import 'core-js/es/number';
import '@patternfly/react-core/dist/styles/base.css';
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import reportWebVitals from './reportWebVitals';
import { SentryBoundary } from './sentry-boundary';
import { CodeQuarkus } from './code-quarkus/code-quarkus';
import { ConfigLoader } from './code-quarkus/loaders/config-loader';

ReactDOM.render((
  <ConfigLoader>{config => (
    <SentryBoundary sentryDSN={config.sentryDSN} environment={config.environment}>
      <CodeQuarkus config={config} />
    </SentryBoundary>
  )}</ConfigLoader>
), document.getElementById('root'));

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
