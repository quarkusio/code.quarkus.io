import React from 'react';
import { TagEntry } from './extensions-picker';
import { Dropdown } from 'react-bootstrap';
import { HoverControlledDropdown } from '../../core/components';
import './extension-tag.scss';

export function ExtensionTag(props: { name: string; tagsDef: TagEntry[]; hover: boolean }) {
  const tagDef = props.tagsDef.find(t => t.name === props.name) || {
    name: props.name,
    color: 'black'
  }
  if (tagDef.hide) {
    return <React.Fragment/>;
  }
  let s = props.name.split(':');
  const key = s.length > 1 ? s[1] : s[0];
  const displayText = tagDef.showFullName ? props.name : key;

  // Check if we should render with full name (name:value split)
  const hasFullName = tagDef.showFullName && s.length > 1;

  let tag;
  if (hasFullName) {
    const valueStyle: {backgroundColor?: string; color?: string} = {};
    if (tagDef.background) {
      valueStyle.backgroundColor = tagDef.background;
    }
    if (tagDef.color) {
      valueStyle.color = tagDef.color;
    }

    tag = (
      <>
        <span className="extension-tag-name">{s[0].toUpperCase()}</span>
        <span className="extension-tag-value" style={valueStyle}>{s[1].toUpperCase()}</span>
      </>
    );
  } else {
    tag = (
      <>
        {displayText.toUpperCase()}
      </>
    );
  }

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

  // Only apply single-color styles if not using full name mode
  if (!hasFullName) {
    if (tagDef.background) {
      style.backgroundColor = tagDef.background;
    }

    if (tagDef.color) {
      style.color = tagDef.color;
    }
  }

  const containerClass = `extension-tag ${props.name.toLowerCase().replace(":", "-")}${hasFullName ? ' extension-tag-full-name' : ''}`;

  return (
    <HoverControlledDropdown
      className="extension-tag-dropdown"
      placement="right"
      delay={{ show: 200, hide: 0 }}
    >
      <Dropdown.Toggle as="div"
        className={containerClass}
        style={style}>{tag}</Dropdown.Toggle>
      <Dropdown.Menu>{tooltip}</Dropdown.Menu>
    </HoverControlledDropdown>
  );
}
