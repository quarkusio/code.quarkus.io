import React, { SetStateAction, useEffect, useRef, useState } from 'react';
import _ from 'lodash';
import { useHotkeys } from 'react-hotkeys-hook';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { InputProps } from '@quarkusio/code-quarkus.core.types';
import {
  debouncedComputeResults,
  ExtensionValues,
  FilterResult,
  processExtensionsValues
} from './extensions-utils';
import { QuarkusProject } from '../api/model';
import './extensions-picker.scss';
import { ExtensionRow } from './extension-row';
import { ExtensionSearchBar } from './extension-search-bar';
import { Button } from 'react-bootstrap';
import { FaCaretDown } from 'react-icons/fa';
import { SearchResultsInfo } from './search-results-info';

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
  platform: boolean;
}

export interface TagEntry {
  name: string;
  href?: string;
  description?: string;
  color?: string;
  hide?: boolean;
}

export interface ExtensionsPickerValue {
  extensions: ExtensionEntry[];
}

const REDUCED_SIZE = 100;

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  tagsDef: TagEntry[];
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
  const { filter } = props;
  const [ processedEntries, setProcessedEntries ] = useState<ExtensionValues[]>([]);
  const [ keyboardActivated, setKeyBoardActivated ] = useState<number>(-1);
  const [ showAll, setShowAll ] = useState<boolean>(false);
  const [ result, setResult ] = useState<FilterResult | undefined>();
  const analytics = useAnalytics();

  function setFilter(filter: string) {
    setKeyBoardActivated(-1);
    setShowAll(false);
    props.setFilter(filter);
  }
  const debouncedSearchEvent = useRef<(events: string[][]) => void>(_.debounce(
    (events) => {
      events.forEach(e => analytics.event(e[0], e[1], e[2]));
    }, 3000)).current;

  const extensions = props.value.extensions || [];

  const entrySet = new Set(extensions.map(e => e.id));
  const entriesById: Map<string, ExtensionEntry> = new Map(props.entries.map(item => [ item.id, item ]));

  useEffect(() => {
    setProcessedEntries(processExtensionsValues(props.entries));
  }, [ props.entries, setProcessedEntries ]);
  
  useEffect(() => {
    debouncedComputeResults(filter, props.entries, processedEntries, setResult);
  }, [ filter, processedEntries, props.entries, setShowAll, setResult ]);

  const allEntries = result?.selected || [];
  const entries = showAll ? allEntries : allEntries.slice(0, REDUCED_SIZE)
  useEffect(() => {
    if (filter.length > 0) {
      const topEvents = entries.slice(0, 5).map(r => [ 'Extension', 'Display in search top 5 results', r.id ]);
      debouncedSearchEvent([ ...topEvents, [ 'UX', 'Search', filter ] ]);
    }
  }, [ filter, entries, debouncedSearchEvent ]);


  const add = (index: number, origin: string) => {
    const id =entries[index].id;
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

  const flip = (index: number, origin: string) => {
    if (!entries[index] || entries[index].default) {
      return;
    }
    if (entrySet.has(entries[index].id)) {
      remove(entries[index].id, origin);
    } else {
      add(index, origin);
    }
  };


  useHotkeys('esc', () => setKeyBoardActivated(-1), hotkeysOptions);
  useHotkeys('up', () => setKeyBoardActivated((prev) => Math.max(0, prev - 1)), hotkeysOptions);
  useHotkeys('down', () => setKeyBoardActivated((prev) => Math.min(entries.length - 1, prev + 1)), hotkeysOptions, [ entries ]);
  useHotkeys('space', (event) => {
    if (keyboardActivated >= 0) {
      event.preventDefault();
      flip(keyboardActivated, 'Keyboard');
    }
  }, hotkeysOptions, [ entries, keyboardActivated ]);

  let currentCat: string | undefined;

  return (
    <div className="extensions-picker" aria-label="Extensions picker">
      <div className="control-container">
        <ExtensionSearchBar placeholder={props.placeholder} filter={filter} project={props.project}
          setFilter={setFilter} result={result}/>
      </div>
      <div className="main-container responsive-container">
        <SearchResultsInfo filter={props.filter} setFilter={props.setFilter} result={result} />
        <div className="extension-list-wrapper">
          {entries.map((ex, i) => {
            const ext = (
              <ExtensionRow
                selected={entrySet.has(ex.id)}
                keyboardActived={i === keyboardActivated}
                {...ex}
                key={i}
                tagsDef={props.tagsDef}
                onClick={() => flip(i, 'List')}
                buildTool={props.buildTool}
                pickerLayout={true}
              />
            );
            if (!result.filtered && (!currentCat || currentCat !== ex.category)) {
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
        {!showAll && allEntries.length > 100 && <Button className='button-show-more btn-base' onClick={() => setShowAll(true)}><FaCaretDown /> Show more extensions</Button>}
      </div>
    </div>
  );
};

