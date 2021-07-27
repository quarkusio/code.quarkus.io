import _ from 'lodash';
import React, { SetStateAction, useEffect, useRef, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { InputProps, useAnalytics } from '../../core';
import { processEntries } from './extensions-picker-utils';
import { QuarkusProject } from '../api/model';
import './extensions-picker.scss';
import { ExtensionRow } from './extension-row';
import { Button } from 'react-bootstrap';
import { ExtensionSearchBar } from './extension-search-bar';

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
  bom?: string
}

export interface ExtensionsPickerValue {
  extensions: ExtensionEntry[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  buildTool: string;
  project?: QuarkusProject;

  filter: string;
  setFilter: React.Dispatch<SetStateAction<string>>;

  filterFunction?(d: ExtensionEntry): boolean;
}

const hotkeysOptions = {
  filter: (e) => {
    const el = (e.target) as any | undefined;
    if (!el) {
      return true;
    }
    const tagName = el && el.tagName;
    return el.id === 'extensions-search-input' || !(tagName === 'INPUT' || tagName === 'SELECT' || tagName === 'TEXTAREA');
  },
  enableOnTags: [ 'INPUT' ] as 'INPUT'[]
};

export const ExtensionsPicker = (props: ExtensionsPickerProps) => {
  const { filter, setFilter } = props;
  const [ keyboardActivated, setKeyBoardActivated ] = useState<number>(-1);
  const analytics = useAnalytics();
  const debouncedSearchEvent = useRef<(events: string[][]) => void>(_.debounce(
    (events) => {
      events.forEach(e => analytics.event(e[0], e[1], e[2]));
    }, 3000)).current;

  const extensions = props.value.extensions || [];
  const entrySet = new Set(extensions.map(e => e.id));
  const entriesById: Map<string, ExtensionEntry> = new Map(props.entries.map(item => [ item.id, item ]));

  const result = processEntries(filter, props.entries);

  useEffect(() => {
    if (filter.length > 0) {
      const topEvents = result.slice(0, 5).map(r => [ 'Extension', 'Display in search top 5 results', r.id ]);
      debouncedSearchEvent([ ...topEvents, [ 'UX', 'Search', filter ] ]);
    }
  }, [ filter, result, debouncedSearchEvent ]);


  const add = (index: number, origin: string) => {
    const id = result[index].id;
    entrySet.add(id);
    props.onChange({ extensions: Array.from(entrySet).map(e => entriesById.get(e)!) });
    analytics.event('UX', 'Extension - Select', origin);
    if (keyboardActivated >= 0) {
      setKeyBoardActivated(index);
    }
  };

  const remove = (id: string, origin: string) => {
    entrySet.delete(id);
    props.onChange({ extensions: Array.from(entrySet).map(e => entriesById.get(e)!) });
    analytics.event('UX', 'Extension - Unselect', origin);
  };

  const clearFilterButton = () => {
    setFilter('');
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


  useHotkeys('esc', () => setKeyBoardActivated(-1), hotkeysOptions);
  useHotkeys('up', () => setKeyBoardActivated((prev) => Math.max(0, prev - 1)), hotkeysOptions);
  useHotkeys('down', () => setKeyBoardActivated((prev) => Math.min(result.length - 1, prev + 1)), hotkeysOptions, [ result ]);
  useHotkeys('space', (event) => {
    if (keyboardActivated >= 0) {
      event.preventDefault();
      flip(keyboardActivated, 'Keyboard');
    }
  }, hotkeysOptions, [ result, keyboardActivated ]);

  let currentCat: string | undefined;
  return (
    <div className="extensions-picker" aria-label="Extensions picker">
      <div className="control-container">
        <ExtensionSearchBar placeholder={props.placeholder} filter={filter} project={props.project}
          setFilter={setFilter} setKeyBoardActivated={setKeyBoardActivated}/>
      </div>
      <div className="main-container responsive-container">
        {!!filter && (
          <div className="extension-search-clear">
            Search results (<Button as="a" onClick={clearFilterButton}>Clear search</Button>)
          </div>
        )}
        <div className="extension-list-wrapper">
          {result.map((ex, i) => {
            const ext = (
              <ExtensionRow
                selected={entrySet.has(ex.id)}
                keyboardActived={i === keyboardActivated}
                {...ex}
                key={i}
                onClick={() => flip(i, 'List')}
                buildTool={props.buildTool}
                pickerLayout={true}
              />
            );
            if (!filter && (!currentCat || currentCat !== ex.category)) {
              currentCat = ex.category;
              return (
                <React.Fragment key={i}>
                  <div className="extension-category">
                    {currentCat}
                  </div>
                  {ext}
                </React.Fragment>
              );
            }
            return ext;
          })}
        </div>
      </div>
    </div>
  );
};

