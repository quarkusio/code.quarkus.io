import React from 'react';
import { TagEntry } from './extensions-picker';
import { Dropdown } from 'react-bootstrap';
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
  const key = s.length > 1 ? s[1] : s[0];
  const tag = (
    <
    >
      {key.toUpperCase()}
    </>
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

  const style: {border?: string; backgroundColor?: string; color?: string} = {};
  if (tagDef.border) {
    style.border = '1px solid ' + tagDef.border;
  }

  if (tagDef.background) {
    style.backgroundColor = tagDef.background;
  }

  if (tagDef.color) {
    style.color = tagDef.color;
  }

  return (
    <HoverControlledDropdown
      className="extension-tag-dropdown"
      placement="right"
      delay={{ show: 200, hide: 0 }}
    >
      <Dropdown.Toggle as="div"
        className={`extension-tag ${props.name.toLowerCase().replace(":", "-")}`}
        style={style}>{tag}</Dropdown.Toggle>
      <Dropdown.Menu>{tooltip}</Dropdown.Menu>
    </HoverControlledDropdown>
  );
}