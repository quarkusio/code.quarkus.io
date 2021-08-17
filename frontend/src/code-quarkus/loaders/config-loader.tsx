import React from 'react';
import { fetchConfig } from '../api/code-quarkus-api';
import { DataLoader } from '../../core';
import { Config } from '../api/model';

export function ConfigLoader(props: { children: (config: Config) => any }) {
  return (
    <DataLoader loader={fetchConfig}>
      {props.children}
    </DataLoader>
  );
}