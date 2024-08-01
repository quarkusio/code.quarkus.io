import * as React from 'react';
import {useHotkeys} from 'react-hotkeys-hook';
import {useAnalytics} from '../../core/analytics';
import {InputProps} from '../../core/types';
import {debouncedComputeResults, FilterResult, ProcessedExtensions, processExtensionsValues} from './extensions-utils';
import {Platform, QuarkusProject} from '../api/model';
import './extensions-picker.scss';
import {ExtensionRow} from './extension-row';
import {ExtensionSearchBar} from './extension-search-bar';
import {Button} from 'react-bootstrap';
import {FaAngleDown, FaAngleLeft, FaAngleRight, FaRocket} from 'react-icons/fa';
import {SearchResultsInfo} from './search-results-info';
import {PresetsPanel} from "./presets-panel";
import _ from 'lodash';
import {SelectedExtensions} from "./selected-extensions";

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
  tagsDef: TagEntry[];
  placeholder: string;
  buildTool: string;
  project?: QuarkusProject;
  platform: Platform;

  filter: string;
  setFilter: React.Dispatch<React.SetStateAction<string>>;

  filterFunction?(d: ExtensionEntry): boolean;
}

const hotkeysOptions = {
  ignoreEventWhen: (e) => {
    const el = (e.target) as any | undefined;
    if (!el) {
      return false;
    }
    const tagName = el && el.tagName;
    return !(el.id === 'extensions-search-input' || tagName !== 'INPUT');
  },
  enableOnFormTags: ['INPUT'] as 'INPUT'[]
};

export const ExtensionsPicker = (props: ExtensionsPickerProps) => {
  const {filter} = props;
  const [processedExtensions, setProcessedExtensions] = React.useState<ProcessedExtensions | undefined>(undefined);
  const [keyboardActivated,  setKeyBoardActivated] = React.useState<number>(-1);
  const [showList, setShowList] = React.useState<boolean>(false);
  const [showAll, setShowAll] = React.useState<boolean>(false);
  const [result, setResult] = React.useState<FilterResult | undefined>();
  const analytics = useAnalytics();
  const context = {element: 'extension-picker'};

  function setFilter(filter: string) {
    setKeyBoardActivated(-1);
    setShowAll(false);
    setShowList(false);
    props.setFilter(filter);
  }

  const extensions = props.value.extensions || [];

  const entrySet = new Set(extensions.map(e => e.id));
  const entriesById: Map<string, ExtensionEntry> = new Map(props.platform.extensions.map(item => [item.id, item]));

  React.useEffect(() => {
    setProcessedExtensions(processExtensionsValues(props.platform.extensions));
  }, [props.platform.extensions, setProcessedExtensions]);

  React.useEffect(() => {
    debouncedComputeResults(analytics, filter, props.platform.extensions, processedExtensions, setResult);
  }, [filter, processedExtensions, props.platform.extensions, setShowAll, setResult]);

  const allEntries = result?.effective || [];
  const entries = showAll ? allEntries : allEntries.slice(0, REDUCED_SIZE)

  const addById = (id: string, type: string) => {
    entrySet.add(id);
    props.onChange({extensions: Array.from(entrySet).map(e => entriesById.get(e)!)});
    analytics.event('Select extension', {extension: id, type, ...context});
  };

  const add = (index: number, type: string) => {
    const id = entries[index].id;
    addById(id, type);
    if (keyboardActivated >= 0) {
      setKeyBoardActivated(index);
    }
  };


  const remove = (id: string, type: string) => {
    entrySet.delete(id);
    props.onChange({extensions: Array.from(entrySet).map(e => entriesById.get(e)!)});
    analytics.event('Unselect extension', {extension: id, type, ...context});
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
  useHotkeys('up', () => {
    setKeyBoardActivated((prev) => Math.max(0, prev - 1))
  }, hotkeysOptions);
  useHotkeys('down', () => setKeyBoardActivated((prev) => Math.min(entries.length - 1, prev + 1)), hotkeysOptions, [entries]);
  useHotkeys('space', (event) => {
    if (keyboardActivated >= 0) {
      event.preventDefault();
      flip(keyboardActivated, 'Keyboard');
    }
  }, hotkeysOptions, [entries, keyboardActivated]);

  let currentCat: string | undefined;

  function toggleShowList() {
    setKeyBoardActivated(-1);
    setShowList(!showList);
  }

  function removeById(id: string, type: string) {
    props.onChange({extensions: _.filter(props.value.extensions, e => e.id !== id && id !== '*')});
    analytics.event('Unselect extension', {extension: id, type, element: 'extension-picker'});
  }

  return (
    <div className="extensions-picker" aria-label="Extensions picker">
      <div className="control-container">
        <ExtensionSearchBar placeholder={props.placeholder} filter={filter} project={props.project}
                            setFilter={setFilter} result={result}/>
      </div>
      <div className="main-container responsive-container">
        {!result?.filtered && !showList ? (
          <div className="extension-picker-summary">
            <div className="toggle-list"><Button className='button-toggle-list btn-light' aria-label="Toggle full list of extensions"
                                                    onClick={toggleShowList}><FaRocket/>View the full list of available
              extensions<FaAngleRight/></Button></div>
            {props.project.extensions.length === 0 ?
              <PresetsPanel platform={props.platform} select={addById}/> :
              <SelectedExtensions extensions={extensions} tagsDef={props.tagsDef} remove={removeById} layout="picker"/>
            }
          </div>

        ) : (
          <div className="extension-picker-list">
            <SearchResultsInfo filter={props.filter} setFilter={props.setFilter} result={result}/>
            {!result?.filtered &&
                <Button className='button-toggle-list btn-light' aria-label="Toggle full list of extensions" onClick={toggleShowList}><FaAngleLeft/>Back</Button>}
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
                    layout="picker"
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
            {!showAll && allEntries.length > 100 &&
                <Button className='button-show-more btn-light' onClick={() => setShowAll(true)}><FaAngleDown/> Show more
                    extensions</Button>}
          </div>
        )}

      </div>
    </div>
  );
};

