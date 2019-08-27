import { GAVPicker } from '@launcher/component';
import { Button } from '@patternfly/react-core';
import React, { SetStateAction } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { ExtensionsLoader } from './extensions-loader';
import './form.scss';
import { QuarkusProject } from './launcher-quarkus';
import { ExtensionEntry, ExtensionsPicker } from './pickers/extensions-picker';

interface QuarkusFormProps {
  project: QuarkusProject;
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  onSave: () => void;
}

export function LauncherQuarkusForm(props: QuarkusFormProps) {
  const setProject = props.setProject;
  const setMetadata = (metadata: any) => setProject((prev) => ({ ...prev, metadata }));
  const setExtensions = (val: { extensions: string[] }) => setProject((prev) => ({ ...prev, extensions: val.extensions }));

  useHotkeys('alt+enter', props.onSave);
  return (
    <div className="launcher-quarkus-form">
      <div className="form-header-sticky-container">
        <div className="form-header responsive-container">
          <div className="project-info">
            <div className="title">
              <h3>Application Info</h3>
            </div>
            <GAVPicker.Element value={props.project.metadata} onChange={setMetadata} visibleFields={['groupId', 'artifactId', 'version', 'packageName']} mode="horizontal" />
          </div>
          <div className="generate-project">
            <Button aria-label="Generate your application" className="generate-button" onClick={props.onSave}>Generate your application (alt + ⏎)</Button>
          </div>
        </div>
      </div>
      <div className="project-extensions responsive-container">
        <div className="title">
          <h3>Extensions</h3>
        </div>
        <ExtensionsLoader name="extensions">
          {extensions => (
            <ExtensionsPicker.Element
              entries={extensions as ExtensionEntry[]}
              value={{ extensions: props.project.extensions }}
              onChange={setExtensions}
              placeholder="RESTEasy, Hibernate ORM, Web..."
            />
          )}
        </ExtensionsLoader>
      </div>

    </div>
  );
}