import { Button } from '@patternfly/react-core';
import React, { SetStateAction, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import './quarkus-project-edition-form.scss';
import { ExtensionEntry, ExtensionsPicker } from '../pickers/extensions-picker';
import { InfoPicker, isValidInfo } from '../pickers/info-picker';
import { GenerateMoreButton } from './generate-more-button';
import { Config, QuarkusProject } from '../api/model';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  extensions: ExtensionEntry[];
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  config: Config;
  onSave: () => void;
}


export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [isProjectValid, setIsProjectValid] = useState(isValidInfo(props.project.metadata));
  const setProject = props.setProject;
  const setMetadata = (metadata: any) => {
    setIsProjectValid(isValidInfo(metadata));
    setProject((prev) => ({ ...prev, metadata }));
  };
  const setExtensions = (value: { extensions: ExtensionEntry[] }) => setProject((prev) => ({ ...prev, extensions: value.extensions }));
  const save = () => {
    if (isProjectValid) {
      props.onSave();
    }
  };
  useHotkeys('alt+enter', save, [isProjectValid, props.onSave]);
  const keyName = window.navigator.userAgent.toLowerCase().indexOf('mac') > -1 ? '⌥' : 'alt';
  return (
    <div className="quarkus-project-edition-form">
      <div className="form-header-sticky-container">
        <div className="form-header responsive-container">
          <div className="project-info">
            <div className="title">
              <h3>Configure your application details</h3>
            </div>
            <InfoPicker value={props.project.metadata} onChange={setMetadata} quarkusVersion={props.config.quarkusVersion}/>
          </div>
          <div className="generate-project">
            <Button aria-label="Generate your application" isDisabled={!isProjectValid} className="generate-button" onClick={save}>Generate your application ({keyName} + ⏎)</Button>
            <GenerateMoreButton project={props.project} githubClientId={props.config.gitHubClientId}/>
          </div>
        </div>
      </div>
      <div className="project-extensions responsive-container">
        <div className="title">
          <h3>Extensions</h3>
        </div>
        <ExtensionsPicker
          entries={props.extensions as ExtensionEntry[]}
          value={{ extensions: props.project.extensions }}
          onChange={setExtensions}
          placeholder="RESTEasy, Hibernate ORM, Web..."
          buildTool={props.project.metadata.buildTool}
        />
      </div>

    </div>
  );
}