import { DependenciesPicker, EnumLoader, MavenSettingsPicker, DependencyItem, Separator } from '@launcher/component';
import React, { useState, Fragment } from 'react';
import { Button } from '@patternfly/react-core';
import { HotKeys } from 'react-hotkeys';
import { DependencyListPicker } from './dependency-list-picker';

interface QuarkusFormProps {
  onSave: (project: QuarkusProject) => void;
}

const validator = () => true;

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
      groupId: 'com.example',
      artifactId: 'quarkus-app',
      version: 'latest',
      name: 'quarkus-app',
      description: 'My application with Quarkus',
      packageName: 'com.example.quarkus-app',
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
          <EnumLoader name="quarkus-extensions">
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
          </EnumLoader>
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