import React from 'react';
import { fetchConfig } from './backend-api';
import { DataLoader } from '../core';

export interface Config {
  environment: string;
  gaTrackingId?: string;
  sentryDSN?: string;
  quarkusVersion: string;
  gitCommitId: string;
  features: string[];
}

export function ConfigLoader(props: { children: (config: Config) => any }) {
  return (
    <DataLoader loader={fetchConfig}>
      {props.children}
    </DataLoader>
  );
}