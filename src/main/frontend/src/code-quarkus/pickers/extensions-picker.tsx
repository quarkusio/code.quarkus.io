import { Button, FormGroup, TextInput, Tooltip } from "@patternfly/react-core";
import { CheckSquareIcon, OutlinedSquareIcon, SearchIcon, TrashAltIcon } from "@patternfly/react-icons";
import React, { useState } from "react";
import { InputProps, useAnalytics } from '../../core';
import { CopyToClipboard } from '../copy-to-clipboard';
import { processEntries } from './extensions-picker-helpers';
import { QuarkusBlurb } from '../quarkus-blurb';
import './extensions-picker.scss';
import _ from 'lodash';


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
  extensions: string[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  buildTool: string;
  filterFunction?(d: ExtensionEntry): boolean;
}

interface ExtensionProps extends ExtensionEntry {
  selected: boolean;
  detailed?: boolean;
  buildTool: string;
  onClick(id: string): void;
}

function Extension(props: ExtensionProps) {
  const [active, setActive] = useState(false);
  const activate = () => setActive(true);
  const desactivate = () => setActive(false);
  const onClick = () => {
    props.onClick(props.id);
    setActive(false);
  };

  const activationEvents = {
    onClick,
    onMouseEnter: activate,
    onMouseLeave: desactivate,
  };

  const description = props.description || '...';
  const descTooltip = <div><b>{props.name}</b><p>{description}</p></div>;
  const tooltip = props.detailed ?
    <div>{props.selected ? 'Remove' : 'Add'} the extension <b>{props.name}</b></div> : descTooltip;
  const buildTool = props.buildTool || 'MAVEN';
  const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
  const addGradleExt = `./gradlew addExtension --extensions="${props.id}"`;
  const addExtCmd = buildTool === 'GRADLE' ? addGradleExt : addMvnExt;
  return (
    <div className={`${active ? 'active' : ''} ${props.selected ? 'selected' : ''} extension-item`}>
      {props.detailed && (
        <div
          className="extension-selector"
          {...activationEvents}
          aria-label={`Switch ${props.id} extension`}
        >
          {!props.selected && !active && <OutlinedSquareIcon />}
          {(active || props.selected) && <CheckSquareIcon />}
        </div>
      )}
      <Tooltip position="bottom" content={tooltip} exitDelay={0} zIndex={100}>
        <div
          className="extension-name"
          {...activationEvents}
        >{props.name}</div>
      </Tooltip>
      {!props.detailed && (
        <div
          className="extension-remove"
          {...activationEvents}
        >
          {active && props.selected && <TrashAltIcon />}
        </div>
      )}
      {props.detailed && (
        <div className="extension-details">
          <Tooltip position="bottom" content={descTooltip} exitDelay={0} zIndex={100}>
            <div
              className="extension-description"
              {...activationEvents}
            >{description}</div>
          </Tooltip>
          <Tooltip position="left" maxWidth="700px" content={<span>Copy {_.upperFirst(buildTool.toLowerCase())} command to clipboard: <br /><code>$ {addExtCmd}</code></span>} exitDelay={0} zIndex={100}>
            <div className="extension-gav"><CopyToClipboard eventId="Add-Extension-Command" content={addExtCmd} /></div>
          </Tooltip>
        </div>
      )}
    </div>
  )
}

export const ExtensionsPicker = (props: ExtensionsPickerProps) => {
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
  const result = processEntries(filter, props.entries);
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
                  buildTool={props.buildTool}
                  key={i}
                  onClick={entrySet.has(ex) ? remove : add}
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
                {...ex}
                key={i}
                buildTool={props.buildTool}
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