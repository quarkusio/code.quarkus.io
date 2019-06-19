import React from 'react';
import { DataLoader } from '@launcher/component';
import extensions from './extensions.json';

interface Extension {
    
}

export function EnumLoader(props: { name: string, children: (items: PropertyValue[]) => any }) {
  const loader = async () => {
    return extensions;
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}