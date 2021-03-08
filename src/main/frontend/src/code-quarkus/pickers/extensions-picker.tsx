import { Button, Dropdown, DropdownItem, DropdownPosition, FormGroup, KebabToggle, TextInput, Tooltip } from '@patternfly/react-core';
import { CheckSquareIcon, EllipsisVIcon, MapIcon, OutlinedSquareIcon, SearchIcon, TrashAltIcon } from '@patternfly/react-icons';
import classNames from 'classnames';
import hotkeys from 'hotkeys-js';
import _ from 'lodash';
import React, { SetStateAction, KeyboardEvent, useEffect, useRef, useState, useCallback } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { InputProps, useAnalytics, CopyToClipboard } from '../../core';
import { QuarkusBlurb } from '../layout/quarkus-blurb';
import { processEntries } from './extensions-picker-utils';
import { QuarkusProject } from '../api/model';
import './extensions-picker.scss';
import { debouncedSyncParamsQuery } from '../api/quarkus-project-utils';

export interface ExtensionEntry {
  id: string;
  name: string;
  version: string;
  keywords: string[];
  tags: string[];
  description?: string;
  shortName?: string;
  category: string;
  order: number;
  default: boolean;
  guide?: string;
}

export interface ExtensionsPickerValue {
  extensions: ExtensionEntry[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  buildTool: string;
  project?: QuarkusProject;

  filterParam?: string;
  setFilterParam?: React.Dispatch<SetStateAction<string>>;

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

function StatusTag(props: { status?: string }) {
  if (!props.status) {
    return <React.Fragment/>;
  }

  switch (props.status) {
    case 'preview':
      return (<span
        className="extension-tag preview"
        title="This is work in progress. API or configuration properties might change as the extension matures. Give us your feedback :)"
      >PREVIEW</span>);
    case 'experimental':
      return (<span
        className="extension-tag experimental"
        title="Early feedback is requested to mature the idea. There is no guarantee of stability nor long term presence in the platform until the solution matures."
      >EXPERIMENTAL</span>);
    case 'deprecated':
      return (<span
          title="This extension has been deprecated. It is likely to be replaced or removed in a future version of Quarkus."
          className="extension-tag deprecated"
      >DEPRECATED</span>);
    case 'provides-example':
      return (<span
        title="This extension provides example code to help you get started..."
        className="extension-tag example"
      ><span className="codestart-example-icon" /></span>);
    default:
      return <React.Fragment/>;
  }
}

function More(props: ExtensionEntry) {
  const [isMoreOpen, setIsMoreOpen] = useState(false);
  const analytics = useAnalytics();
  const gav = `${props.id}:${props.version}`;
  const gavArray = gav.split(':');
  const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
  const addGradleExt = `./gradlew addExtension --extensions="${props.id}"`;
  const xml = `<dependency>\n            <groupId>${gavArray[0]}</groupId>\n            <artifactId>${gavArray[1]}</artifactId>\n        </dependency>`;
  const closeMore = () => {
    setTimeout(() => setIsMoreOpen(false), 1000);
  };

  const openGuide = () => {
    analytics.event('Extension', 'Click "Open Extension Guide" link', props.id);
    closeMore();
  };

  const moreItems = [
    (
      <DropdownItem key="maven" variant="icon">
        <CopyToClipboard event={['Extension', 'Copy the command to add it with Maven', props.id]} content={addMvnExt}
                         tooltipPosition="left" onClick={closeMore} zIndex={201}
        >Copy the command to add it with Maven</CopyToClipboard>
      </DropdownItem>
    ),
    (
      <DropdownItem key="gradle" variant="icon">
        <CopyToClipboard event={['Extension', 'Copy the command to add it with Gradle', props.id]}
                         content={addGradleExt} tooltipPosition="left" onClick={closeMore} zIndex={201}
        >Copy the command to add it with Gradle</CopyToClipboard>
      </DropdownItem>
    ),
    (
      <DropdownItem key="xml" variant="icon">
        <CopyToClipboard event={['Extension', 'Copy the extension pom.xml snippet', props.id]} content={xml}
                         tooltipPosition="left" onClick={closeMore} zIndex={201}
        >Copy the extension pom.xml snippet</CopyToClipboard>
      </DropdownItem>
    ),
    (
      <DropdownItem key="id" variant="icon">
        <CopyToClipboard event={['Extension', 'Copy the GAV', props.id]} content={gav} tooltipPosition="left"
                         onClick={closeMore} zIndex={201}>Copy the extension GAV</CopyToClipboard>
      </DropdownItem>
    )
  ];

  if (props.guide) {
    moreItems.push(
      <DropdownItem key="guide" variant="icon" href={props.guide} target="_blank" onClick={openGuide}>
        <MapIcon/> Open Extension Guide
      </DropdownItem>
    );
  }

  return (
    <Dropdown
      isOpen={isMoreOpen}
      position={DropdownPosition.left}
      toggle={<KebabToggle onToggle={() => setIsMoreOpen(!isMoreOpen)}/>}
      onClick={(e) => e.stopPropagation()}
      dropdownItems={moreItems}
    />
  );
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
  const selected = props.selected || props.default;

  return (
    <div {...activationEvents} className={classNames('extension-item', {
      'keyboard-actived': props.keyboardActived,
      hover,
      selected,
      'by-default': props.default
    })}>
      {props.detailed && (
        <div
          className="extension-selector"
          aria-label={`Switch ${props.id} extension`}
        >
          {!selected && !(hover) && <OutlinedSquareIcon/>}
          {(hover || selected) && <CheckSquareIcon/>}
        </div>
      )}

      <div className="extension-summary">
        <span className="extension-name" title={`${props.name} (${props.version})`}>{props.name}</span>
        {props.tags && props.tags.map((s, i) => <StatusTag key={i} status={s}/>)}
      </div>

      {!props.detailed && (
        <div
          className="extension-remove"
        >
          {hover && props.selected && <TrashAltIcon/>}
        </div>
      )}

      {props.detailed && (
        <div className="extension-details">
          <div
            className="extension-description" title={description}
          >{description}</div>
          <div className="extension-more">
            {!hover && (
              <button aria-label="Actions" className="pf-c-dropdown__toggle" type="button" aria-expanded="false">
                <EllipsisVIcon/>
              </button>
            )}
            {hover && <More {...props} />}
          </div>
        </div>
      )}
    </div>
  );
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
  const entriesById: Map<string, ExtensionEntry> = new Map(props.entries.map(item => [item.id, item]));

  hotkeys.filter = (e) => {
    const el = (e.target) as any | undefined;
    if (!el) {
      return true;
    }
    const tagName = el && el.tagName;
    return el.id === 'extension-search' || !(tagName === 'INPUT' || tagName === 'SELECT' || tagName === 'TEXTAREA');
  };
  const result = processEntries(filter, props.entries);

  const addParamToFilter = useCallback(() => {
    const extensionSearch = props.filterParam || '';

    setFilter(extensionSearch);
    debouncedSyncParamsQuery(extensionSearch, props.project);
  }, [props.filterParam, props.project]);

  useEffect(() => {
    addParamToFilter();
  }, [addParamToFilter]);

  useEffect(() => {
    if (filter.length > 0) {
      const topEvents = result.slice(0, 5).map(r => ['Extension', 'Display in search top 5 results', r.id]);
      debouncedSearchEvent([...topEvents, ['UX', 'Search', filter]]);
    }
  }, [filter, result, debouncedSearchEvent]);

  const setFilterParam = (value: string) => {
    if (props.setFilterParam) {
      props.setFilterParam(value);
    }
  };
  const add = (index: number, origin: string) => {
    const id = result[index].id;
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
    analytics.event('UX', 'Extension - Unselect', origin);
  };
  const onSearchKeyDown = (e: KeyboardEvent) => {
    if (e.which === 38 || e.which === 40) {
      e.preventDefault();
    }
  };
  const search = (f: string) => {
    setKeyBoardActived(-1);
    setFilter(f);
    setFilterParam(f);
  };
  const clearFilterButton = () => {
    setFilter('');
    setFilterParam('');
  };

  const flip = (index: number, origin: string) => {
    if (!result[index] || result[index].default) {
      return;
    }
    if (entrySet.has(result[index].id)) {
      remove(result[index].id, origin);
    } else {
      add(index, origin);
    }
  };


  useHotkeys('esc', () => setKeyBoardActived(-1));
  useHotkeys('up', () => setKeyBoardActived((prev) => Math.max(0, prev - 1)));
  useHotkeys('down', () => setKeyBoardActived((prev) => Math.min(result.length - 1, prev + 1)), [result]);
  useHotkeys('space', (event) => {
    if (keyboardActived >= 0) {
      event.preventDefault();
      flip(keyboardActived, 'Keyboard');
    }
  }, [result, keyboardActived]);

  const categories = new Set(props.entries.map(i => i.category));
  let currentCat: string | undefined;
  return (
    <div className="extensions-picker" aria-label="Extensions picker">
      <div className="control-container">
        <div className="title">
          <h3>Pick your extensions</h3>
        </div>
        <Tooltip position="bottom" exitDelay={0} zIndex={100} content={`${Array.from(categories).join(', ')}`}>
          <FormGroup
            fieldId="search-extensions-input"
          >
            <SearchIcon/>
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
                  onClick={() => remove(ex.id, 'Selection')}
                />
              ))
            }
          </div>
        </div>
      </div>
      <div className="main-container">
        <QuarkusBlurb/>
        {!!filter && (
          <div className="extension-search-clear">
            Search results (<Button variant="link" onClick={clearFilterButton}>Clear search</Button>)
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
                onClick={() => flip(i, 'List')}
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
              );
            }
            return ext;
          })}
        </div>
      </div>
    </div>
  );
};
