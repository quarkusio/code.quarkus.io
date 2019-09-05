import { DataLoader } from '@launcher/component';
import React from 'react';
import { fetchConfig } from './backend-api';

export interface Config {
  environment: string;
  gaTrackingId?: string;
  sentryDSN?: string;
  quarkusVersion: string;
  gitCommitId: string;
}

export function ConfigLoader(props: { children: (config: Config) => any }) {
  return (
    <DataLoader loader={fetchConfig}>
      {props.children}
    </DataLoader>
  );
}