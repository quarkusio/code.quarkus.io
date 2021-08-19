import React from 'react';
import { DataLoader } from '../../core';
import { fetchPlatform } from '../api/code-quarkus-api';
import { Platform } from '../api/model';

export function PlatformLoader(props: { name: string, children: (platform: Platform) => any }) {
  return (
    <DataLoader loader={fetchPlatform}>
      {props.children}
    </DataLoader>
  );
}