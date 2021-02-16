import React, { SetStateAction, useState } from 'react';
import './quarkus-project-edition-form.scss';
import { ExtensionEntry, ExtensionsPicker } from '../pickers/extensions-picker';
import { InfoPicker, isValidInfo } from '../pickers/info-picker';
import { GenerateButton } from './generate-button';
import { Config, QuarkusProject } from '../api/model';
import { Target } from '../api/quarkus-project-utils';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  extensions: ExtensionEntry[];
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  config: Config;
  onSave: (target?: Target) => void;

  filterParam?: string;
  setFilterParam?: React.Dispatch<SetStateAction<string>>;
}


export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [isProjectValid, setIsProjectValid] = useState(isValidInfo(props.project.metadata));
  const setProject = props.setProject;
  const setMetadata = (metadata: any) => {
    setIsProjectValid(isValidInfo(metadata));
    setProject((prev) => ({ ...prev, metadata }));
  };
  const setExtensions = (value: { extensions: ExtensionEntry[] }) => setProject((prev) => ({ ...prev, extensions: value.extensions }));
  const save = (target?: Target) => {
    if (isProjectValid) {
      props.onSave(target);
    }
  };
  return (
    <div className="quarkus-project-edition-form">
      <div className="form-header-sticky-container">
        <div className="form-header responsive-container">
          <div className="project-info">
            <div className="title">
              <h3>
                Configure your application details
              </h3>
            </div>
            <InfoPicker value={props.project.metadata} onChange={setMetadata} quarkusVersion={props.config.quarkusVersion}/>
          </div>
          <div className="generate-project">
            <GenerateButton project={props.project} generate={save} isProjectValid={isProjectValid} githubClientId={props.config.gitHubClientId}/>
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
          filterParam={props.filterParam}
          setFilterParam={props.setFilterParam}
          project={props.project}
        />
      </div>

    </div>
  );
}