import React, { Fragment } from 'react';
import _ from 'lodash';
import { Button, Checkbox, FormGroup, Title, Modal } from '@patternfly/react-core';
import { InputProps } from '@launcher/component';

import './extension-list-picker.scss';
import { ExtensionEntry } from './extensions-picker';

export interface ExtensionListPickerValue {
  extensions?: string[];
}

interface ExtensionListPickerProps extends InputProps<ExtensionListPickerValue> {
  isOpen: boolean;
  close: () => void;
  extensions: ExtensionEntry[];
}

export function ExtensionsListPicker(props: ExtensionListPickerProps) {
  const extensionGroups = _.groupBy(props.extensions, 'metadata.category');
  const extensions = props.value.extensions || [];
  const entrySet = new Set(extensions);
  const add = (id: string) => {
    if (!entrySet.has(id)) {
      entrySet.add(id);
    } else {
      entrySet.delete(id);
    }
    props.onChange({ extensions: Array.from(entrySet) });
  };
return (
    <Modal
      title="List of extensions for Quarkus"
      isOpen={props.isOpen}
      isLarge={false}
      onClose={props.close}
      aria-label="List of extensions for Quarkus"
      actions={[
        <Button key="launch-new" variant="secondary" aria-label="Close" onClick={props.close}>
          Close
        </Button>,
      ]}
    >
      <div className="extensions-list">
        {
          Object.keys(extensionGroups).map((name, groupIndex) => (
            <Fragment key={groupIndex}>
              <Title size="lg">{name}</Title>
              {
                extensionGroups[name].map(ext => (
                  <FormGroup fieldId={ext.id} key={ext.id}>
                    <Checkbox
                      id={ext.id}
                      name={ext.id}
                      isChecked={entrySet.has(ext.id)}
                      onChange={() => add(ext.id)}
                      aria-label={`Choose ${ext.name}`}
                      label={(
                        <Fragment>
                          <b>{ext.name}</b><div>{ext.description}</div>
                        </Fragment>
                      )}
                    />
                  </FormGroup>
                ))
              }
            </Fragment>
          ))
        }
      </div>
    </Modal>
  );
}