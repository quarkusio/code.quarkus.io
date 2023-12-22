import React from 'react';
import { TagEntry } from './extensions-picker';
import { Dropdown, Overlay } from 'react-bootstrap';
import { HoverControlledDropdown } from '../../core/components';

export function ExtensionTags(props: { name?: string; tagsDef: TagEntry[]; hover: boolean }) {
  if (!props.name) {
    return <React.Fragment/>;
  }
  const tagDef = props.tagsDef.find(t => t.name === props.name) || {
    name: props.name,
    color: 'black'
  }
  if (tagDef.hide) {
    return <React.Fragment/>;
  }
  let s = props.name.split(':');
  const name = s.length > 1 ? s[1] : s[0];
  const tag = (
    <span 
    >
      {name.toUpperCase()}
    </span>
  );
  const tooltip = !props.hover ? '': (
    <>
      [{props.name}] - {tagDef.description}
      {!!tagDef.href && (
        <> 
          {tagDef.description && <br/>}
          <a onClick={(e) => e.stopPropagation()} href={tagDef.href} target="_blank" rel="noopener noreferrer" >More info...</a>
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
        className={`extension-tag ${props.name.toLowerCase().replace(":", "-")}`}
        style={{ borderColor: tagDef.color!! }}>{tag}</Dropdown.Toggle>
      <Dropdown.Menu>{tooltip}</Dropdown.Menu>
    </HoverControlledDropdown>
  );
}