import React from 'react';
import { DataLoader } from '../core';
import { ExtensionEntry } from './pickers/extensions-picker';
import { fetchExtensions } from './backend-api';

interface Extension {
  id: string;
  name: string;
  labels: string[];
  description?: string;
  shortName?: string;
  category: string;
  default: boolean;
  order: number;
}

export function ExtensionsLoader(props: { name: string, children: (entries: ExtensionEntry[]) => any }) {
  return (
    <DataLoader loader={fetchExtensions}>
      {props.children}
    </DataLoader>
  );
}