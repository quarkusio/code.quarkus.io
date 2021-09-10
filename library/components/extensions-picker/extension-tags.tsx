import React from 'react';
import { TagEntry } from './extensions-picker';

export function ExtensionTags(props: { name?: string; tagsDef: TagEntry[] }) {
  if (!props.name) {
    return <React.Fragment/>;
  }
  const tagDef = props.tagsDef.find(t => t.name === props.name) || {
    name: props.name,
    color: "black"
  }
  if(!!tagDef.href) {
    return <a href={tagDef.href} target="_blank" rel="noopener noreferrer"  className={`extension-tag ${props.name.toLowerCase()}`} title={tagDef.description}
              style={{borderColor: tagDef.color}}>{props.name.toUpperCase()}</a>
  }
  return (<span
    className={`extension-tag ${props.name.toLowerCase()}`}
    title={tagDef.description}
    style={{borderColor: tagDef.color}}
  >{props.name.toUpperCase()}</span>);
}