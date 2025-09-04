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
  keyboardFocus?: boolean;
  layout?: 'picker' | 'cart';
  buildTool?: string;
  tagsDef: TagEntry[];
  transitive?:  boolean;

  onClick?(id: string): void;
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
    if (props.transitive) {
      return;
    }
    props.onClick(props.id);
    setHover(false);
  };

  const activationEvents = {
    onClick,
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => setHover(false),
  };

  useEffect(() => {
    if (props.keyboardFocus) {
      scrollIntoView();
    }
  }, [ props.keyboardFocus ])

  const description = props.description || '...';
  const transitive = props.transitive;
  const selected = props.selected || props.default;
  const ga = props.id.split(':');
  const id = ga[1] + (props.platform ? '' : `:${props.version}`);
  return (
    <div {...activationEvents} className={classNames('extension-row', {
      'keyboard-focus': props.keyboardFocus,
      hover,
      transitive,
      selected,
      'by-default': props.default
    })} ref={ref} aria-label={props.id} >
      {props.layout === 'picker' && (
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
        <span className="extension-id" title={props.id}> [{id}]</span>
        <ExtensionsOrigin platform={props.platform} />
        {props.tags && props.tags.map((s, i) => <ExtensionTags key={i} tagsDef={props.tagsDef} name={s} hover={hover}/>)}
      </div>

      {props.layout === 'cart' && !props.transitive && (
        <div
          className="extension-remove"
        >
          {hover && props.selected && <FaTrashAlt/>}
        </div>
      )}

      {props.layout === 'picker' && (
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
