import { Button, FormGroup, TextInput, Tooltip } from "@patternfly/react-core";
import { CheckSquareIcon, OutlinedSquareIcon, SearchIcon, TrashAltIcon } from "@patternfly/react-icons";
import classNames from 'classnames';
import hotkeys from 'hotkeys-js';
import React, { useState, KeyboardEvent } from "react";
import { useHotkeys } from 'react-hotkeys-hook';
import { InputProps, useAnalytics } from '../../core';
import { CopyToClipboard } from '../copy-to-clipboard';
import { QuarkusBlurb } from '../quarkus-blurb';
import { processEntries } from './extensions-picker-helpers';
import './extensions-picker.scss';


export interface ExtensionEntry {
  id: string;
  name: string;
  keywords: string[];
  description?: string;
  shortName?: string;
  category: string;
  order: number,
  default: boolean
}

export interface ExtensionsPickerValue {
  extensions: string[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  filterFunction?(d: ExtensionEntry): boolean;
}

interface ExtensionProps extends ExtensionEntry {
  selected: boolean;
  keyboardActived: boolean;
  detailed?: boolean;
  default: boolean;
  onClick(id: string): void;
}

function Extension(props: ExtensionProps) {
  const [hover, setHover] = useState(false);
  const onClick = () => {
    if (props.default) {
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

  const description = props.description || '...';
  const descTooltip = props.default ? <div><b>{props.name}</b><p>{description}<i>(This extension is included by default)</i></p></div> : <div><b>{props.name}</b><p>{description}</p></div>;
  let tooltip = props.detailed && !props.default ?
    <div>{props.selected ? 'Remove' : 'Add'} the extension <b>{props.name}</b></div> : descTooltip;

  const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
  const selected = props.selected || props.default;
  return (
    <div {...activationEvents} className={classNames('extension-item', { 'keyboard-actived': props.keyboardActived, hover, selected, readonly: props.default })}>
      {props.detailed && (
        <div
          className="extension-selector"
          aria-label={`Switch ${props.id} extension`}
        >
          {!selected && !(hover) && <OutlinedSquareIcon />}
          {(hover || selected) && <CheckSquareIcon />}
        </div>
      )}
      <Tooltip position="bottom" content={tooltip} exitDelay={0} zIndex={100}>
        <div
          className="extension-name"
        >{props.name}</div>
      </Tooltip>
      {!props.detailed && (
        <div
          className="extension-remove"
        >
          {hover && props.selected && <TrashAltIcon />}
        </div>
      )}
      {props.detailed && (
        <div className="extension-details">
          <Tooltip position="bottom" content={descTooltip} exitDelay={0} zIndex={100}>
            <div
              className="extension-description"
            >{description}</div>
          </Tooltip>
          <div className="extension-gav"><CopyToClipboard eventId="Add-Extension-Command" content={addMvnExt} tooltipPosition="left" /></div>
        </div>
      )}
    </div>
  )
}

export const ExtensionsPicker = (props: ExtensionsPickerProps) => {
  const [filter, setFilter] = useState('');
  const [hasSearched, setHasSearched] = useState(false);
  const [keyboardActived, setKeyBoardActived] = useState<number>(-1);
  const analytics = useAnalytics();
  const extensions = props.value.extensions || [];
  const entrySet = new Set(extensions);
  const entriesById: Map<String, ExtensionEntry> = new Map(props.entries.map(item => [item.id, item]));

  hotkeys.filter = (e) => {
    const el = (e.target || e.srcElement) as any | undefined;
    if (!el) {
      return true;
    }
    var tagName = el && el.tagName;
    return el.id === 'extension-search' || !(tagName === 'INPUT' || tagName === 'SELECT' || tagName === 'TEXTAREA');
  };
  const result = processEntries(filter, props.entries);

  const add = (index: number) => {
    const id = result[index].id
    entrySet.add(id);
    props.onChange({ extensions: Array.from(entrySet) });
    analytics.event('Picker', 'Add-Extension', id);
    if(keyboardActived >= 0) {
      setKeyBoardActived(index);
    } 
  };

  const remove = (id: string) => {
    entrySet.delete(id);
    props.onChange({ extensions: Array.from(entrySet) });
    analytics.event('Picker', 'Remove-Extension', id)
  };
  const onSearchKeyDown = (e: KeyboardEvent) => {
    if (e.which === 38 || e.which === 40) {
      e.preventDefault();
    }
  };
  const search = (f: string) => {
    if (!hasSearched) {
      analytics.event('Picker', 'Search-Extension')
    }
    setHasSearched(true);
    setKeyBoardActived(-1);
    setFilter(f);
  }

  const flip = (index: number) => {
    if(!result[index]) {
      return;
    }
    if (entrySet.has(result[index].id)) {
      remove(result[index].id);
    } else {
      add(index);
    }
  }


  useHotkeys('esc', () => setKeyBoardActived(-1));
  useHotkeys('up', () => setKeyBoardActived(Math.max(0, keyboardActived - 1)), [keyboardActived]);
  useHotkeys('down', () => setKeyBoardActived(Math.min(result.length - 1, keyboardActived + 1)), [result, keyboardActived]);
  useHotkeys('space', (event) => {
    if (keyboardActived >= 0) {
      event.preventDefault();
      flip(keyboardActived);
    }
  }, [result, keyboardActived]);

  const categories = new Set(props.entries.map(i => i.category));
  let currentCat: string | undefined = undefined;
  return (
    <div className="extensions-picker" aria-label="Extensions picker">
      <div className="control-container">
        <div className="title">
          <h3>Extensions</h3>
        </div>
        <Tooltip position="bottom" exitDelay={0} zIndex={100} content={`${Array.from(categories).join(', ')}`}>
          <FormGroup
            fieldId="search-extensions-input"
          >
            <SearchIcon />
            <TextInput
              id="extension-search"
              type="search"
              onKeyDown={onSearchKeyDown}
              aria-label="Search extensions"
              placeholder={props.placeholder}
              className="search-extensions-input"
              value={filter}
              onChange={search}
            />
          </FormGroup>
        </Tooltip>
        <div className={`selected-extensions`}>
          <h4>Selected Extensions</h4>
          <div className={`extension-list`}>
            {
              extensions.map((ex, i) => (
                <Extension
                  selected={entrySet.has(ex)}
                  keyboardActived={i === keyboardActived}
                  {...entriesById.get(ex)!}
                  key={i}
                  onClick={() => remove(ex)}
                />
              ))
            }
          </div>
        </div>
      </div>
      <div className="result-container">
        <QuarkusBlurb />
        {!!filter && (
          <div className="extension-search-clear">
            Search results (<Button variant="link" onClick={() => setFilter('')}>Clear search</Button>)
            </div>
        )}
        <div className="list-container">
          {result.map((ex, i) => {
            const ext = (
              <Extension
                selected={entrySet.has(ex.id)}
                keyboardActived={i === keyboardActived}
                {...ex}
                key={i}
                onClick={() => flip(i)}
                detailed
              />
            );
            if (!filter && (!currentCat || currentCat !== ex.category)) {
              currentCat = ex.category;
              return (
                <div style={{ display: 'contents' }} key={i}>
                  <div className="extension-category">
                    {currentCat}
                  </div>
                  {ext}
                </div>
              )
            }
            return ext;
          })}
        </div>
      </div>
    </div>
  );
}
