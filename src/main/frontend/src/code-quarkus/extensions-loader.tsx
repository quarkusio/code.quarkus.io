import React from 'react';
import { DataLoader } from '../core';
import { ExtensionEntry } from './pickers/extensions-picker';
import { fetchExtensions } from './backend-api';

interface Extension {
  id: string;
  shortId: string;
  version: string;
  name: string;
  keywords: string[];
  tags: string[];
  description?: string;
  shortName?: string;
  category: string;
  default: boolean;
  order: number;
  guide?: string;
}

export function ExtensionsLoader(props: { name: string, children: (entries: ExtensionEntry[]) => any }) {
  return (
    <DataLoader loader={fetchExtensions}>
      {props.children}
    </DataLoader>
  );
}