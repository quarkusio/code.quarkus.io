import { Grid, GridItem, Stack, StackItem, TextInput, Title, Tooltip } from "@patternfly/react-core";
import { PlusIcon, TimesIcon } from "@patternfly/react-icons";
import React, { useState } from "react";
import './extensions-picker.scss';
import { InputProps, useAnalytics, Picker } from '@launcher/component';

export interface ExtensionEntry {
  id: string;
  name: string;
  description: string;
  metadata: { category: string; };
}

export interface ExtensionsPickerValue {
  extensions?: string[];
}

interface ExtensionsPickerProps extends InputProps<ExtensionsPickerValue> {
  entries: ExtensionEntry[];
  placeholder: string;
  filterFunction?(d: ExtensionEntry): boolean;
}

enum OperationType {
  Add = 1,
  Remove,
}

interface ExtensionProps extends ExtensionEntry {
  operation?: OperationType;
  onClick(id: string): void;
}

function Extension(props:ExtensionProps) {
  const [active, setActive] = useState(false);
  const onClick = () => {
    props.onClick(props.id);
  };

  return (
    <div
      className={`${active ? 'active' : ''} extension-item`}
      onMouseEnter={() => setActive(true)}
      onMouseLeave={() => setActive(false)}
      onClick={onClick}
    >
      <Stack style={{ position: 'relative' }}>
        <StackItem isMain>
          <Title size="sm" aria-label={`Pick ${props.id} extension`}>{props.name}</Title>
          <span className="extension-category">{props.metadata.category}</span>
          {active && (props.operation === OperationType.Add ?
            <PlusIcon className="extension-icon" /> : <TimesIcon className="extension-icon" />)}
        </StackItem>
        <StackItem isMain={false}>{props.description}</StackItem>
      </Stack>
    </div>
  )
}

export const ExtensionsPicker: Picker<ExtensionsPickerProps, ExtensionsPickerValue> = {
  checkCompletion: (value: ExtensionsPickerValue) => !!value.extensions && value.extensions.length > 0,
  Element: (props: ExtensionsPickerProps) => {
    const [filter, setFilter] = useState('');
    const analytics = useAnalytics();
    const extensions = props.value.extensions || [];
    const entrySet = new Set(extensions);
    const entriesById = new Map(props.entries.map(item => [item.id, item]));

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

    const filterFunction = (d: ExtensionEntry) =>
      filter !== '' && (d.description.toLowerCase().includes(filter.toLowerCase())
        || d.name.toLowerCase().includes(filter.toLowerCase())
        || d.metadata.category.toLowerCase().includes(filter.toLowerCase()));
    const result = props.entries.filter(filterFunction);
    const categories = new Set(props.entries.map(i => i.metadata.category));
    return (
      <Grid gutter="md" className="extensions-picker">
        <GridItem sm={12} md={6}>
          <Tooltip position="right" content={`${Array.from(categories).join(', ')}`}>
            <TextInput
              aria-label="Search extensions"
              placeholder={props.placeholder}
              className="search-extensions-input"
              value={filter}
              onChange={value => setFilter(value)}
            />
          </Tooltip>
          <div aria-label="Select extensions" className={`available-extensions`}>
            {
              result.map((ex, i) => (
                <Extension
                  operation={OperationType.Add}
                  {...ex}
                  key={i}
                  onClick={add}
                />
              ))
            }
            {filter && !result.length && <Title size="xs" style={{ paddingTop: '10px' }}>No result.</Title>}
          </div>
        </GridItem>
        {extensions.length > 0 && (
          <GridItem sm={12} md={6} >
            <Title size="md">Selected:</Title>
            <div className={`selected-extensions`}>
              {
                extensions.map((selected, i) => (
                  <Extension
                    operation={OperationType.Remove}
                    {...entriesById.get(selected)!}
                    key={i}
                    onClick={remove}
                  />
                ))
              }
            </div>
          </GridItem>
        )}
      </Grid>
    );
  }
}