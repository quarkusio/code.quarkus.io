import { Button, FormGroup, TextInput, Tooltip } from '@patternfly/react-core';
import { SearchIcon } from '@patternfly/react-icons';
import hotkeys from 'hotkeys-js';
import _ from 'lodash';
import React, { KeyboardEvent, SetStateAction, useCallback, useEffect, useRef, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { InputProps, useAnalytics } from '../../core';
import { QuarkusBlurb } from '../layout/quarkus-blurb';
import { processEntries } from './extensions-picker-utils';
import { QuarkusProject } from '../api/model';
import './extensions-picker.scss';
import { debouncedSyncParamsQuery } from '../api/quarkus-project-utils';
import { ExtensionRow } from './extension-row';

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
                <ExtensionRow
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
              <ExtensionRow
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
