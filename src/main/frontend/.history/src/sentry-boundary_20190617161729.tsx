import * as Sentry from '@sentry/browser';
import * as React from 'react';
import { Component, ErrorInfo } from 'react';
import { sentryDsn } from './app/config';

if (sentryDsn) {
  console.info('Sentry is enabled');
  Sentry.init({
    dsn: sentryDsn
  });
} else {
  console.info('Sentry is disabled');
}

export class SentryBoundary extends Component<{}, { error?: Error }> {
  constructor(props: {}) {
    super(props);
    this.state = {error: undefined};
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    if(sentryDsn) {
      this.setState({error});
      Sentry.withScope(scope => {
        Object.keys(errorInfo).forEach(key => {
          // @ts-ignore
          scope.setExtra(key, errorInfo[key]);
        });
        Sentry.captureException(error);
      });
    }
  }

  public render() {
    if (this.state.error) {
      return (
        <button onClick={() => Sentry.showReportDialog()}>Report feedback</button>
      );
    } else {
      return this.props.children;
    }
  }
}
