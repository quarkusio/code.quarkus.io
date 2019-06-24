import { DependenciesPicker, DependencyItem, MavenSettingsPicker, Separator } from '@launcher/component';
import { Button } from '@patternfly/react-core';
import React, { Fragment, useState } from 'react';
import { HotKeys } from 'react-hotkeys';
import { DependencyListPicker } from './dependency-list-picker';
import { ExtensionsLoader } from './extensions-loader';

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
  dependencies: string[];
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
    dependencies: [],
  });


  const setMetadata = (metadata: any) => setProject((prev) => ({ ...prev, metadata }));
  const setDependencies = (val: { dependencies: string[] }) => setProject((prev) => ({ ...prev, dependencies: val.dependencies }));
  return (
    <div className="quarkus-form-container">
      <HotKeys style={{outline: 0}}
        keyMap={{ save: 'alt+enter' }}
        handlers={{ save: () => props.onSave(project) }}
      >
        <div className="brand-image" />
        <div className="row">
          <div className="header">
            <h3>Project Metadata</h3>
          </div>
          <div className="form">
            <MavenSettingsPicker.Element value={project.metadata} onChange={setMetadata} />
          </div>
        </div>
        <div className="row">
          <div className="header"></div>
          <div className="form">
            <Separator />
          </div>
        </div>
        <div className="row">
          <ExtensionsLoader name="quarkus-extensions">
            {extensions => (
              <Fragment>
                <div className="header">
                  <h3>Extensions</h3>
                  <Button variant="link" onClick={() => setOpen(true)}>See all</Button>
                  <DependencyListPicker
                    isOpen={open}
                    close={close}
                    extensions={extensions as DependencyItem[]}
                    value={{ dependencies: project.dependencies }}
                    onChange={setDependencies}
                  />
                </div>
                <div className="form">
                  <DependenciesPicker.Element
                    items={extensions as DependencyItem[]}
                    value={{ dependencies: project.dependencies }}
                    onChange={setDependencies}
                    placeholder="RESTEasy, Hibernate ORM, Web..."
                  />
                </div>
              </Fragment>
            )}
          </ExtensionsLoader>
        </div>
        <div className="row footer">
          <div className="header"></div>
          <div className="form">
            <Button onClick={() => props.onSave(project)}>Generate Project - alt + ⏎</Button>
          </div>
        </div>
      </HotKeys>
    </div>
  );
}