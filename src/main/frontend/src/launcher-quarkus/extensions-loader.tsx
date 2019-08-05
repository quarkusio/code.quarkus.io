import React from 'react';
import { DataLoader } from '@launcher/component';
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
    try {
      const data = await fetch('/api/quarkus/extensions');
      return await data.json();
    } catch(e) {
      throw new Error("Failed to load Quarkus extension list");
    }   
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}