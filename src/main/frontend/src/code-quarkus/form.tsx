import { Button } from '@patternfly/react-core';
import React, { SetStateAction, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { ExtensionsLoader } from './extensions-loader';
import './form.scss';
import { QuarkusProject } from './code-quarkus';
import { ExtensionEntry, ExtensionsPicker } from './pickers/extensions-picker';
import { InfoPicker } from './pickers/info-picker';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  onSave: () => void;
}

export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [isMetadataValid, setIsMetadataValid] = useState(false);
  const setProject = props.setProject;
  const setMetadata = (metadata: any, isValid: boolean) => {
    setIsMetadataValid(isValid);
    setProject((prev) => ({ ...prev, metadata }));
  };
  const setExtensions = (val: { extensions: string[] }) => setProject((prev) => ({ ...prev, extensions: val.extensions }));

  useHotkeys('alt+enter', props.onSave);
  return (
    <div className="code-quarkus-form">
      <div className="form-header-sticky-container">
        <div className="form-header responsive-container">
          <div className="project-info">
            <div className="title">
              <h3>Application Info</h3>
            </div>
            <InfoPicker value={props.project.metadata} isValid={isMetadataValid} onChange={setMetadata} />
          </div>
          <div className="generate-project">
            <Button aria-label="Generate your application" isDisabled={!isMetadataValid} className="generate-button" onClick={props.onSave}>Generate your application (alt + ⏎)</Button>
          </div>
        </div>
      </div>
      <div className="project-extensions responsive-container">
        <div className="title">
          <h3>Extensions</h3>
        </div>
        <ExtensionsLoader name="extensions">
          {extensions => (
            <ExtensionsPicker
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