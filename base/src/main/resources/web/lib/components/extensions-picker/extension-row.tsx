import React, { useEffect, useRef, useState } from 'react';
import classNames from 'classnames';
import { ExtensionEntry, TagEntry } from './extensions-picker';
import { ExtensionTags } from './extension-tags';
import { ExtensionMoreDropdown } from './extension-more-dropdown';
import { FaRegCheckSquare, FaRegSquare, FaTrashAlt } from 'react-icons/fa';
import './extension-row.scss';
import { ExtensionsOrigin } from './extensions-origin';

export interface ExtensionRowProps extends ExtensionEntry {
  selected?: boolean;
  keyboardActived?: boolean;
  pickerLayout?: boolean;
  buildTool?: string;
  tagsDef: TagEntry[];

  onClick(id: string): void;
}

export function ExtensionRow(props: ExtensionRowProps) {
  const [ hover, setHover ] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  function scrollIntoView() {
    if (ref.current) {
      ref.current.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
    }
  }

  const onClick = () => {
    props.onClick(props.id);
    setHover(false);
  };

  const activationEvents = {
    onClick,
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => setHover(false),
  };

  useEffect(() => {
    if (props.keyboardActived) {
      scrollIntoView();
    }
  }, [ props.keyboardActived ])

  const description = props.description || '...';
  const selected = props.selected || props.default;
  const ga = props.id.split(':');
  return (
    <div {...activationEvents} className={classNames('extension-row', {
      'keyboard-actived': props.keyboardActived,
      hover,
      selected,
      'by-default': props.default
    })} ref={ref} aria-label={props.id} >
      {props.pickerLayout && (
        <div
          className="extension-selector"
          aria-label={`Switch ${props.id} extension`}
        >
          {!selected && !(hover) && <FaRegSquare/>}
          {(hover || selected) && <FaRegCheckSquare/>}
        </div>
      )}

      <div className="extension-summary">
        <span className="extension-name" title={`${props.name} (${props.version})`}>{props.name}</span>
        <span className="extension-id" title={props.id}> [{ga[1]}]</span>
        <ExtensionsOrigin platform={props.platform} />
        {props.tags && props.tags.map((s, i) => <ExtensionTags key={i} tagsDef={props.tagsDef} name={s} hover={hover}/>)}
      </div>

      {!props.pickerLayout && (
        <div
          className="extension-remove"
        >
          {hover && props.selected && <FaTrashAlt/>}
        </div>
      )}

      {props.pickerLayout && (
        <React.Fragment>
          <div
            className="extension-description" title={description}
          >{description}</div>
          <div className="extension-more">
            <ExtensionMoreDropdown {...props} active={hover}/>
          </div>
        </React.Fragment>
      )}
    </div>
  );
}
