import React from 'react';
import { DataLoader } from '@launcher/component';
import extensions from './extensions.json';
import { ExtensionEntry } from './pickers/extensions-picker.jsx';

interface Extension {
  id: string;
  name: string;
  labels: string[];
  description?: string;
  shortName?: string;
  category: string;
}

export function ExtensionsLoader(props: { name: string, children: (entries: ExtensionEntry[]) => any }) {
  const loader = async () => {
    return extensions.map(e => ({...e, labels: new Set(e.labels)}));
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}