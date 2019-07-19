import { MavenSettingsPicker } from '@launcher/component';
import { Button } from '@patternfly/react-core';
import React, { useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { ExtensionsLoader } from './extensions-loader';
import { ExtensionEntry, ExtensionsPicker } from './pickers/extensions-picker';
import './quarkus-form.scss';

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
  const setExtensions = (val: { extensions: string[] }) => setProject((prev) => ({ ...prev, extensions: val.extensions }));

  useHotkeys('alt+enter', () => props.onSave(project));
  return (
    <div className="quarkus-form-container">
      <div className="form-header">
        <div className="form-header-content responsive-container">
          <div className="form-section project-info">
            <div className="title">
              <h3>Project Info</h3>
            </div>
            <MavenSettingsPicker.Element value={project.metadata} onChange={setMetadata} visibleFields={['groupId', 'artifactId', 'version', 'packageName']} mode="horizontal" />
          </div>
          <div className="form-section generate-project">
            <Button aria-label="Generate project" onClick={() => props.onSave(project)}>Generate Project (alt + ⏎)</Button>
          </div>
        </div>
      </div>
      <div className="form-section project-extensions responsive-container">
        <div className="title">
          <h3>Extensions</h3>
        </div>
        <ExtensionsLoader name="extensions">
          {extensions => (
            <ExtensionsPicker.Element
              entries={extensions as ExtensionEntry[]}
              value={{ extensions: project.extensions }}
              onChange={setExtensions}
              placeholder="RESTEasy, Hibernate ORM, Web..."
            />
          )}
        </ExtensionsLoader>
      </div>

    </div>
  );
}