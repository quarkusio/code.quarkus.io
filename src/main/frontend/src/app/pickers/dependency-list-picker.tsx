import React, { Fragment } from 'react';
import _ from 'lodash';
import { Button, Checkbox, FormGroup, Title, Modal } from '@patternfly/react-core';
import { DependencyItem } from '@launcher/component';
import { InputProps } from '@launcher/component';

import './dependency-list-picker.scss';

export interface DependenciesPickerValue {
  dependencies?: string[];
}

interface DependencyListPickerProps extends InputProps<DependenciesPickerValue> {
  isOpen: boolean;
  close: () => void;
  extensions: DependencyItem[];
}

export function DependencyListPicker(props: DependencyListPickerProps) {
  const dependencyGroups = _.groupBy(props.extensions, 'metadata.category');
  const dependencies = props.value.dependencies || [];
  const dependenciesSet = new Set(dependencies);
  const addDep = (id: string) => {
    if (!dependenciesSet.has(id)) {
      dependenciesSet.add(id);
    } else {
      dependenciesSet.delete(id);
    }
    props.onChange({ dependencies: Array.from(dependenciesSet) });
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
      <div className="dependencyList">
        {
          Object.keys(dependencyGroups).map((name, groupIndex) => (
            <Fragment key={groupIndex}>
              <Title size="lg">{name}</Title>
              {
                dependencyGroups[name].map(dep => (
                  <FormGroup fieldId={dep.id} key={dep.id}>
                    <Checkbox
                      id={dep.id}
                      name={dep.id}
                      isChecked={dependenciesSet.has(dep.id)}
                      onChange={() => addDep(dep.id)}
                      aria-label={`Choose ${dep.name}`}
                      label={(
                        <Fragment>
                          <b>{dep.name}</b><div>{dep.description}</div>
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