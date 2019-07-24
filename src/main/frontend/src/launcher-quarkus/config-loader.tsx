import { DataLoader } from '@launcher/component';
import React from 'react';

export interface Config {
  environment: string;
  gaTrackingId?: string;
}

export function ConfigLoader(props: { children: (config: Config) => any }) {
  const loader = async () => {
    try {
      const data = await fetch('/api/quarkus/config');
      return await data.json();
    } catch(e) {
      return {
        environment: 'dev'
      }
    }    
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}