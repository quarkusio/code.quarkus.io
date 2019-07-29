import { InputProps, Picker, useAnalytics } from '@launcher/component';
import { Button, FormGroup, TextInput, Tooltip } from "@patternfly/react-core";
import { CheckSquareIcon, ClipboardCheckIcon, ClipboardIcon, OutlinedSquareIcon, SearchIcon, TrashAltIcon } from "@patternfly/react-icons";
import copy from 'copy-to-clipboard';
import React, { useState } from "react";
import './extensions-picker.scss';

export interface ExtensionEntry {
  id: string;
  name: string;
  labels: string[];
  description?: string;
  shortName?: string;
  category: string;
  order: number,
}

export interface ExtensionsPickerValue {
  extensions?: string[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  filterFunction?(d: ExtensionEntry): boolean;
}

interface ExtensionProps extends ExtensionEntry {
  selected: boolean;
  detailed?: boolean;
  onClick(id: string): void;
}

function CopyToClipboard(props: { content: string }) {
  const [active, setActive] = useState(false);
  const [copied, setCopied] = useState(false);

  const copyToClipboard = () => {
    copy(props.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 1000);
  }
  return (
    <div
      onMouseEnter={() => setActive(true)}
      onMouseLeave={() => setActive(false)}
      onClick={copyToClipboard}

    >
      {active || copied ? <ClipboardCheckIcon /> : <ClipboardIcon />}
    </div>
  )

}

function Extension(props: ExtensionProps) {
  const [active, setActive] = useState(false);
  const onClick = () => {
    props.onClick(props.id);
    setActive(false);
  };

  const description = props.description || '...';
  const tooltip = props.detailed ?
    `${props.selected ? 'Remove' : 'Add'} the extension '${props.name}'` : `${props.name}: ${description}`;

  return (
    <div
      className={`${active ? 'active' : ''} ${props.selected ? 'selected' : ''} extension-item`}
      onMouseEnter={() => setActive(true)}
      onMouseLeave={() => setActive(false)}
      aria-label={`Switch ${props.id} extension`}
    >
      {props.detailed && (
        <div className="extension-selector" onClick={onClick}>
          {!props.selected && !active && <OutlinedSquareIcon />}
          {(active || props.selected) && <CheckSquareIcon />}
        </div>
      )}
      <Tooltip position="bottom" content={tooltip} exitDelay={0} zIndex={100}>
        <div className="extension-name" onClick={onClick}>{props.name}</div>
      </Tooltip>
      {!props.detailed && (
        <div className="extension-remove" onClick={onClick}>
          {active && props.selected && <TrashAltIcon />}
        </div>
      )}
      {props.detailed && (
        <div className="extension-details">
          <Tooltip position="bottom" content={`${props.name}: ${description}`} exitDelay={0} zIndex={100}>
            <div className="extension-description" onClick={onClick}>{description}</div>
          </Tooltip>
          <Tooltip position="left" content={`Copy '${props.id}' to clipboard`} exitDelay={0} zIndex={100}>
            <div className="extension-gav"><CopyToClipboard content={props.id} /></div>
          </Tooltip>
        </div>
      )}
    </div>
  )
}

export const filterFunction = (filter: string) => (d: ExtensionEntry) => {
  const filterLowerCase = filter.trim().toLowerCase();
  if (!filterLowerCase) {
    return true;
  }
  const shortName = d.shortName ? d.shortName.toLowerCase() : '';
  if (filterLowerCase === shortName) {
    return true;
  }
  return d.name.toLowerCase().includes(filterLowerCase)
    || d.labels.filter(l => l.startsWith(filterLowerCase)).length > 0
    || (d.category && d.category.toLowerCase().startsWith(filterLowerCase))
    || shortName.startsWith(filterLowerCase);
}

export const sortFunction = (filter: string) => (a: ExtensionEntry, b: ExtensionEntry) => {
  const filterLowerCase = filter.trim().toLowerCase();
  if (!filterLowerCase) {
    return a.order > b.order ? 1 : -1;
  }
  const startWithAShortName = !!a.shortName && a.shortName.toLowerCase().startsWith(filterLowerCase);
  const startWithBShortName = !!b.shortName && b.shortName.toLowerCase().startsWith(filterLowerCase);
  if (startWithAShortName !== startWithBShortName) {
    return startWithAShortName ? -1 : 1;
  }
  const startWithOneOfALabel = a.labels.filter(l => l.startsWith(filterLowerCase)).length > 0;
  const startWithOneOfBLabel = b.labels.filter(l => l.startsWith(filterLowerCase)).length > 0;
  if (startWithOneOfALabel !== startWithOneOfBLabel) {
    return startWithOneOfALabel ? -1 : 1;
  }
  if (a.name.toLowerCase().startsWith(filterLowerCase) !== b.name.toLowerCase().startsWith(filterLowerCase)) {
    return a.name.toLowerCase().startsWith(filterLowerCase) ? -1 : 1;
  }
  return a.order > b.order ? 1 : -1;
}

export const ExtensionsPicker: Picker<ExtensionsPickerProps, ExtensionsPickerValue> = {
  checkCompletion: (value: ExtensionsPickerValue) => !!value.extensions && value.extensions.length > 0,
  Element: (props: ExtensionsPickerProps) => {
    const [filter, setFilter] = useState('');
    const [hasSearched, setHasSearched] = useState(false);
    const analytics = useAnalytics();
    const extensions = props.value.extensions || [];
    const entrySet = new Set(extensions);
    const entriesById: Map<String, ExtensionEntry> = new Map(props.entries.map(item => [item.id, item]));

    const add = (id: string) => {
      entrySet.add(id);
      props.onChange({ extensions: Array.from(entrySet) });
      analytics.event('Picker', 'Add-Extension', id);
    };

    const remove = (id: string) => {
      entrySet.delete(id);
      props.onChange({ extensions: Array.from(entrySet) });
      analytics.event('Picker', 'Remove-Extension', id)
    };

    const search = (f: string) => {
      if (!hasSearched) {
        analytics.event('Picker', 'Search-Extension')
      }
      setHasSearched(true);
      setFilter(f);
    }

    const result = props.entries.filter(filterFunction(filter));
    const categories = new Set(props.entries.map(i => i.category));
    let currentCat: string | undefined = undefined;
    return (
      <div className="extensions-picker">
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
                    {...entriesById.get(ex)!}
                    key={i}
                    onClick={entrySet.has(ex) ? remove : add}
                  />
                ))
              }
            </div>
          </div>
        </div>
        <div className="result-container">
          {!!filter && (
            <div className="extension-search-clear">
              Search results (<Button variant="link" onClick={() => setFilter('')}>Clear search</Button>)
            </div>
          )}
          <div className="list-container">
            {result.sort(sortFunction(filter)).map((ex, i) => {
              const ext = (
                <Extension
                  selected={entrySet.has(ex.id)}
                  {...ex}
                  key={i}
                  onClick={entrySet.has(ex.id) ? remove : add}
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
}