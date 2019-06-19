import React from 'react';
import { DataLoader } from '@launcher/component';
import extensions from '../data-examples/mock-git-user.json';

export function EnumLoader(props: { name: string, children: (items: PropertyValue[]) => any }) {
  const loader = async () => {
    return await client.enum(props.name);
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}