import React from 'react';
import { TagEntry } from './extensions-picker';
import { Dropdown, Overlay } from 'react-bootstrap';
import { HoverControlledDropdown } from '@quarkusio/code-quarkus.core.components';

export function ExtensionTags(props: { name?: string; tagsDef: TagEntry[]; hover: boolean }) {
  if (!props.name) {
    return <React.Fragment/>;
  }
  const tagDef = props.tagsDef.find(t => t.name === props.name) || {
    name: props.name,
    color: 'black'
  }
  const tag = (
    <span 
    >
      {props.name.toUpperCase()}
    </span>
  );
  const tooltip = !props.hover ? '': (
    <>
      {tagDef.description}
      {!!tagDef.href && (
        <> 
          {tagDef.description && <br/>}
          <a href={tagDef.href} target="_blank" rel="noopener noreferrer" >More info...</a>
        </>
      )}
    </>
  );

  return (
    <HoverControlledDropdown
      className="extension-tag-dropdown"
      placement="right"
      overlay={Overlay}
      delay={{ show: 200, hide: 0 }}
    >
      <Dropdown.Toggle as="div"
        className={`extension-tag ${props.name.toLowerCase()}`}
        style={{ borderColor: tagDef.color }}>{tag}</Dropdown.Toggle>
      <Dropdown.Menu>{tooltip}</Dropdown.Menu>
    </HoverControlledDropdown>
  );
}