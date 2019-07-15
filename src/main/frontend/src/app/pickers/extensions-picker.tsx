import { InputProps, Picker, useAnalytics } from '@launcher/component';
import { FormGroup, TextInput, Tooltip } from "@patternfly/react-core";
import { CheckIcon, ClipboardCheckIcon, ClipboardIcon, SearchIcon, TrashAltIcon } from "@patternfly/react-icons";
import copy from 'copy-to-clipboard';
import React, { useState } from "react";
import './extensions-picker.scss';

export interface ExtensionEntry {
  id: string;
  name: string;
  labels: Set<String>;
  description?: string;
  shortName?: string;
  category: string;
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

  return (
    <div
      className={`${active ? 'active' : ''} ${props.selected ? 'selected' : ''} extension-item`}
      onMouseEnter={() => setActive(true)}
      onMouseLeave={() => setActive(false)}
      aria-label={`Switch ${props.id} extension`}
    >
      {props.detailed && (
        <div className="extension-selector" onClick={onClick}>
          {((props.selected && !active) || (!props.selected && active)) && <CheckIcon />}
          {(props.selected && active) && <TrashAltIcon />}
        </div>
      )}
      <Tooltip position="top" content={props.name} exitDelay={0}>
        <div className="extension-name" onClick={onClick}>{props.name}</div>
      </Tooltip>
      {!props.detailed && (
        <div className="extension-remove" onClick={onClick}>
          {active && props.selected && <TrashAltIcon />}
        </div>
      )}
      {props.detailed && (
        <div className="extension-details">
          <Tooltip position="top" content={props.description} exitDelay={0}>
            <div className="extension-description" onClick={onClick}>{props.description}</div>
          </Tooltip>
          <Tooltip position="left" content={`Copy '${props.id}' to clipboard`} exitDelay={0}>
            <div className="extension-gav"><CopyToClipboard content={props.id} /></div>
          </Tooltip>
        </div>
      )}
    </div>
  )
}

export const filterFunction = (filter: string, shortNames: Set<String>) => (d: ExtensionEntry) => {
  const filterLowerCase = filter.toLowerCase();
  if (!filterLowerCase) {
    return true;
  }
  const shortName = d.shortName ? d.shortName.toLowerCase() : '';
  if (filterLowerCase === shortName) {
    return true;
  }
  if (shortNames.has(filterLowerCase)) {
    return false;
  }
  return d.name.toLowerCase().includes(filterLowerCase)
    || d.labels.has(filterLowerCase)
    || (d.category && d.category.toLowerCase().startsWith(filterLowerCase))
    || shortName.startsWith(filterLowerCase);
}

export function getShortNames(entries: ExtensionEntry[]) {
  const shortNames = entries
    .map(e => e.shortName && e.shortName.toLowerCase())
    .filter(e => !!e) as string[];
  return new Set(shortNames);
}

export const sortFunction = (filter: string) => (a: ExtensionEntry, b: ExtensionEntry) => {
  const filterLowerCase = filter.toLowerCase();
  if (!filterLowerCase) {
    if (a.category === b.category) {
      return a.name > b.name ? -1 : 1;
    }
    return a.category > b.category ? -1 : 1;
  }
  const startWithAShortName = !!a.shortName && a.shortName.toLowerCase().startsWith(filterLowerCase);
  const startWithBShortName = !!b.shortName && b.shortName.toLowerCase().startsWith(filterLowerCase);
  if (startWithAShortName !== startWithBShortName) {
    return startWithAShortName ? -1 : 1;
  }
  if (a.labels.has(filterLowerCase) !== b.labels.has(filterLowerCase)) {
    return a.labels.has(filterLowerCase) ? -1 : 1;
  }
  if (a.name.toLowerCase().startsWith(filterLowerCase) !== b.name.toLowerCase().startsWith(filterLowerCase)) {
    return a.name.toLowerCase().startsWith(filterLowerCase) ? -1 : 1;
  }
  return a.name < b.name ? -1 : 1;
}

export const ExtensionsPicker: Picker<ExtensionsPickerProps, ExtensionsPickerValue> = {
  checkCompletion: (value: ExtensionsPickerValue) => !!value.extensions && value.extensions.length > 0,
  Element: (props: ExtensionsPickerProps) => {
    const [filter, setFilter] = useState('');
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

    const shortNames = getShortNames(props.entries);

    const result = props.entries.filter(filterFunction(filter, shortNames));
    const categories = new Set(props.entries.map(i => i.category));
    let currentCat: string | undefined = undefined;
    return (
      <div className="extensions-picker">
        <div className="control-container">
          <Tooltip position="right" content={`${Array.from(categories).join(', ')}`}>
            <FormGroup
              fieldId="search-extensions-input"
            >
              <SearchIcon />
              <TextInput
                aria-label="Search extensions"
                placeholder={props.placeholder}
                className="search-extensions-input"
                value={filter}
                onChange={value => setFilter(value)}
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
                <div style={{display: 'contents'}}>
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

    );
  }
}