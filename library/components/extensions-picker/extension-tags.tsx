import React from 'react';
import { TagEntry } from './extensions-picker';

export function ExtensionTags(props: { name?: string; tags: TagEntry[] }) {
  if (!props.name) {
    return <React.Fragment/>;
  }
  const tag = props.tags.find(t => t.name === props.name) || {
    name: props.name,
    color: "black"
  }
  return (<span
    className={`extension-tag ${props.name.toLowerCase()}`}
    title={tag.description}
    style={{borderColor: tag.color}}
  >{props.name.toUpperCase()}</span>);
}