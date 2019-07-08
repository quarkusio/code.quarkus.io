import { DependencyItem, MavenSettingsPicker } from '@launcher/component';
import { Button, FormGroup } from '@patternfly/react-core';
import React, { Fragment, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { ExtensionsLoader } from './extensions-loader';
import { DependencyListPicker } from './pickers/dependency-list-picker';
import { ExtensionEntry, ExtensionsPicker } from './pickers/extensions-picker';

interface QuarkusFormProps {
  onSave: (project: QuarkusProject) => void;
}

export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    description?: string;
    packageName?: string;
  }
  extensions: string[];
}

export function QuarkusForm(props: QuarkusFormProps) {
  const [open, setOpen] = useState(false);
  const close = () => setOpen(false);

  const [project, setProject] = useState<QuarkusProject>({
    metadata: {
      groupId: 'org.example',
      artifactId: 'quarkus-app',
      version: '0.0.1-SNAPSHOT',
      description: 'My application with Quarkus',
      packageName: 'org.example',
    },
    extensions: [],
  });


  const setMetadata = (metadata: any) => setProject((prev) => ({ ...prev, metadata }));
  const setDependencies = (val: { extensions: string[] }) => setProject((prev) => ({ ...prev, extensions: val.extensions }));

  useHotkeys('alt+enter', () => props.onSave(project));
  return (
    <div className="quarkus-form-container">
      <div className="brand-image" />
      <div className="form-body">
        <div className="row">
          <div className="header">
            <h3>Project Metadata</h3>
          </div>
          <div className="form">
            <MavenSettingsPicker.Element value={project.metadata} onChange={setMetadata} visibleFields={['groupId', 'artifactId', 'version', 'packageName']} />
          </div>
        </div>
        <div className="row">
          <ExtensionsLoader name="quarkus-extensions">
            {extensions => (
              <Fragment>
                <div className="header">
                  <h3>Extensions</h3>

                  <DependencyListPicker
                    isOpen={open}
                    close={close}
                    extensions={extensions as DependencyItem[]}
                    value={{ dependencies: project.extensions }}
                    onChange={setDependencies}
                  />
                </div>
                <div className="form">
                  <FormGroup
                    fieldId="extensions-picker"
                    label={<span>Search extensions to include <Button variant="link" onClick={() => setOpen(true)}>(See all)</Button></span>}
                  >
                    <ExtensionsPicker.Element
                      entries={extensions as ExtensionEntry[]}
                      value={{ extensions: project.extensions }}
                      onChange={setDependencies}
                      placeholder="RESTEasy, Hibernate ORM, Web..."
                    />
                  </FormGroup>
                </div>

              </Fragment>
            )}
          </ExtensionsLoader>
        </div>
      </div>
      <div className="row footer">
        <div className="header"></div>
        <div className="form">
          <Button variant="secondary" aria-label="Generate project" onClick={() => props.onSave(project)}>Generate Project - alt + ⏎</Button>
        </div>
      </div>
    </div>
  );
}