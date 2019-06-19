import React from 'react';
import { DataLoader } from '@launcher/component';
import extensions from './extensions.json';

interface Extension {
  id: string;
  name: string;
  description?: string;
  metadata?: any;
}

export function ExtensionsLoader(props: { name: string, children: (items: Extension[]) => any }) {
  const loader = async () => {
    return extensions;
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}