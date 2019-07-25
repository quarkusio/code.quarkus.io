import * as Sentry from '@sentry/browser';
import * as React from 'react';
import { Component, ErrorInfo } from 'react';
import { sentryDsn } from './launcher-quarkus/config';
import { Button } from '@patternfly/react-core';

interface SentryBoundaryProps {
  sentryDsn?: string;
  environment: string;
}


export class SentryBoundary extends Component<SentryBoundaryProps, { error?: Error }> {
  constructor(props: SentryBoundaryProps) {
    super(props);
    this.state = {error: undefined};

    if (props.sentryDsn) {
      console.info('Sentry is enabled');
      Sentry.init({
        dsn: props.sentryDsn,
        environment: props.environment,
      });
    } else {
      console.info('Sentry is disabled');
    }
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
        <Button onClick={() => Sentry.showReportDialog()}>Report feedback</Button>
      );
    } else {
      return this.props.children;
    }
  }
}
