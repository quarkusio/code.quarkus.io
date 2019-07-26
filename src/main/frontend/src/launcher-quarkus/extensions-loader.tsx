import React from 'react';
import { DataLoader } from '@launcher/component';
import extensions from './extensions.json';
import { ExtensionEntry } from './pickers/extensions-picker';

interface Extension {
  id: string;
  name: string;
  labels: string[];
  description?: string;
  shortName?: string;
  category: string;
  order: number,
}

export function ExtensionsLoader(props: { name: string, children: (entries: ExtensionEntry[]) => any }) {
  const loader = async () => {
    return extensions;
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}