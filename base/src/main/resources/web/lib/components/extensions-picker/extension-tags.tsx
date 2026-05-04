import React from 'react';
import { TagEntry } from './extensions-picker';
import { ExtensionTag } from './extension-tag';

export function ExtensionTags(props: { tags?: string[]; tagsDef: TagEntry[]; hover: boolean }) {
  if (!props.tags || props.tags.length === 0) {
    return <React.Fragment/>;
  }
  return (
    <>
      {props.tags.map((tag, i) => (
        <ExtensionTag key={i} name={tag} tagsDef={props.tagsDef} hover={props.hover} />
      ))}
    </>
  );
}