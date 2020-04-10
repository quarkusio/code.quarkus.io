import React from 'react';
import { DataLoader } from '../../core';
import { fetchExtensions } from '../api/code-quarkus-api';
import { Extension } from '../api/model';

export function ExtensionsLoader(props: { name: string, children: (entries: Extension[]) => any }) {
  return (
    <DataLoader loader={fetchExtensions}>
      {props.children}
    </DataLoader>
  );
}