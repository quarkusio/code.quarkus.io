import { Button, Dropdown, DropdownItem, DropdownPosition, FormGroup, KebabToggle, TextInput, Tooltip } from "@patternfly/react-core";
import { CheckSquareIcon, OutlinedSquareIcon, SearchIcon, TrashAltIcon, MapIcon } from "@patternfly/react-icons";
import classNames from 'classnames';
import hotkeys from 'hotkeys-js';
import React, { KeyboardEvent, useState, useRef, useEffect } from "react";
import { useHotkeys } from 'react-hotkeys-hook';
import { InputProps, useAnalytics } from '../../core';
import { CopyToClipboard } from '../copy-to-clipboard';
import { QuarkusBlurb } from '../quarkus-blurb';
import { processEntries } from './extensions-picker-helpers';
import './extensions-picker.scss';
import _ from 'lodash';

export interface ExtensionEntry {
  id: string;
  shortId: string;
  name: string;
  keywords: string[];
  description?: string;
  shortName?: string;
  category: string;
  order: number,
  default: boolean,
  status: string,
  guide?: string,
}

export interface ExtensionsPickerValue {
  extensions: ExtensionEntry[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  buildTool: string;
  filterFunction?(d: ExtensionEntry): boolean;
}

interface ExtensionProps extends ExtensionEntry {
  selected: boolean;
  keyboardActived: boolean;
  detailed?: boolean;
  default: boolean;
  buildTool: string;
  onClick(id: string): void;
}

function Extension(props: ExtensionProps) {
  const [hover, setHover] = useState(false);
  const [isMoreOpen, setIsMoreOpen] = useState(false);
  const analytics = useAnalytics();
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
  const closeMore = () => {
    setTimeout(() => setIsMoreOpen(false), 1000);
  }

  const openGuide = () => {
    analytics && analytics.event('Extension', 'Click "Open Extension Guide" link', props.id);
    closeMore();
  }
  const description = props.description || '...';
  const descTooltip = <div><b>{props.name}</b><p>{description}</p></div>;
  let tooltip = props.detailed && !props.default ?
    <div>{props.selected ? 'Remove' : 'Add'} the extension <b>{props.name}</b></div> : descTooltip;

  const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
  const selected = props.selected || props.default;
  const addGradleExt = `./gradlew addExtension --extensions="${props.id}"`;
  const moreItems = [
    <DropdownItem key="maven" variant="icon">
      <CopyToClipboard event={["Extension", 'Copy the command to add it with Maven', props.id]} content={addMvnExt} tooltipPosition="left" onClick={closeMore} zIndex={201}>Copy the command to add it with Maven</CopyToClipboard>
    </DropdownItem>,
    <DropdownItem key="gradle" variant="icon">
      <CopyToClipboard event={["Extension", 'Copy the command to add it with Gradle', props.id]} content={addGradleExt} tooltipPosition="left" onClick={closeMore} zIndex={201}>Copy the command to add it with Gradle</CopyToClipboard>
    </DropdownItem>,
    <DropdownItem key="id" variant="icon">
      <CopyToClipboard event={["Extension", "Copy the GAV", props.id]} content={props.id} tooltipPosition="left" onClick={closeMore} zIndex={201}>Copy the extension GAV</CopyToClipboard>
    </DropdownItem>
  ];
  if (props.guide) {
    moreItems.push(
      <DropdownItem key="guide" variant="icon" href={props.guide} target="_blank" onClick={openGuide}>
        <MapIcon /> Open Extension Guide
      </DropdownItem>
    );
  }
  return (
    <div {...activationEvents} className={classNames('extension-item', { 'keyboard-actived': props.keyboardActived, hover, selected, 'by-default': props.default })}>
      {props.detailed && (
        <Tooltip position="bottom" content={tooltip} exitDelay={0} zIndex={100}>
          <div
            className="extension-selector"
            aria-label={`Switch ${props.id} extension`}
          >
            {!selected && !(hover) && <OutlinedSquareIcon />}
            {(hover || selected) && <CheckSquareIcon />}
          </div>
        </Tooltip>
      )}

      <div className="extension-summary">
        <Tooltip position="bottom" content={descTooltip} exitDelay={0} zIndex={100}>
          <span
            className="extension-name"
          >{props.name}</span>
        </Tooltip>
        {props.status === 'preview' && <Tooltip position="right" content="This is work in progress. API or configuration properties might change as the extension matures. Give us your feedback :)" exitDelay={0} zIndex={100}><span
          className="extension-tag preview"
        >PREVIEW</span></Tooltip>}
        {props.status === 'experimental' && <Tooltip position="right" content="Early feedback is requested to mature the idea. There is no guarantee of stability nor long term presence in the platform until the solution matures." exitDelay={0} zIndex={100}><span
          className="extension-tag experimental"
        >EXPERIMENTAL</span></Tooltip>}
        {props.default && <Tooltip position="right" content="Applications generated with Code Quarkus are currently demonstrating a Hello World REST endpoint, this extension is therefore included by default to make this use case work." exitDelay={0} zIndex={100}><span
          className="extension-tag default"
        >INCLUDED</span></Tooltip>}
      </div>

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
          <div className="extension-more">
            <Dropdown
              isOpen={isMoreOpen}
              position={DropdownPosition.left}
              toggle={<KebabToggle onToggle={() => setIsMoreOpen(!isMoreOpen)} />}
              onClick={(e) => e.stopPropagation()}
              dropdownItems={moreItems} />
          </div>
        </div>
      )}
    </div>
  )
}

export const ExtensionsPicker = (props: ExtensionsPickerProps) => {
  const [filter, setFilter] = useState('');
  const [keyboardActived, setKeyBoardActived] = useState<number>(-1);
  const analytics = useAnalytics();
  const debouncedSearchEvent = useRef<(events: string[][]) => void>(_.debounce(
    (events) => {
      events.forEach(e => analytics.event(e[0], e[1], e[2]));
    }, 3000)).current;

  const extensions = props.value.extensions || [];
  const entrySet = new Set(extensions.map(e => e.id));
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

  useEffect(() => {
    if (filter.length > 0) {
      const topEvents = result.slice(0, 5).map(r => ['Extension', 'Display in search top 5 results', r.id]);
      debouncedSearchEvent([...topEvents, ['UX', 'Search', filter]]);
    }
  }, [filter, result, debouncedSearchEvent]);
  
  const add = (index: number, origin: string) => {
    const id = result[index].id
    entrySet.add(id);
    props.onChange({ extensions: Array.from(entrySet).map(e => entriesById.get(e)!) });
    analytics.event('UX', 'Extension - Select', origin);
    if (keyboardActived >= 0) {
      setKeyBoardActived(index);
    }
  };

  const remove = (id: string, origin: string) => {
    entrySet.delete(id);
    props.onChange({ extensions: Array.from(entrySet).map(e => entriesById.get(e)!) });
    analytics.event('UX', 'Extension - Unselect', origin)
  };
  const onSearchKeyDown = (e: KeyboardEvent) => {
    if (e.which === 38 || e.which === 40) {
      e.preventDefault();
    }
  };
  const search = (f: string) => {
    setKeyBoardActived(-1);
    setFilter(f);
  }

  const flip = (index: number, origin: string) => {
    if (!result[index] || result[index].default) {
      return;
    }
    if (entrySet.has(result[index].id)) {
      remove(result[index].id, origin);
    } else {
      add(index, origin);
    }
  }


  useHotkeys('esc', () => setKeyBoardActived(-1));
  useHotkeys('up', () => setKeyBoardActived((prev) => Math.max(0, prev - 1)));
  useHotkeys('down', () => setKeyBoardActived((prev) => Math.min(result.length - 1, prev + 1)), [result]);
  useHotkeys('space', (event) => {
    if (keyboardActived >= 0) {
      event.preventDefault();
      flip(keyboardActived, "Keyboard");
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
          <div className="extension-list-wrapper">
            {
              extensions.map((ex, i) => (
                <Extension
                  selected={entrySet.has(ex.id)}
                  keyboardActived={i === keyboardActived}
                  {...ex}
                  buildTool={props.buildTool}
                  key={i}
                  onClick={() => remove(ex.id, "Selection")}
                />
              ))
            }
          </div>
        </div>
      </div>
      <div className="main-container">
        <QuarkusBlurb />
        {!!filter && (
          <div className="extension-search-clear">
            Search results (<Button variant="link" onClick={() => setFilter('')}>Clear search</Button>)
            </div>
        )}
        <div className="extension-list-wrapper">
          {result.map((ex, i) => {
            const ext = (
              <Extension
                selected={entrySet.has(ex.id)}
                keyboardActived={i === keyboardActived}
                {...ex}
                key={i}
                onClick={() => flip(i, "List")}
                buildTool={props.buildTool}
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
