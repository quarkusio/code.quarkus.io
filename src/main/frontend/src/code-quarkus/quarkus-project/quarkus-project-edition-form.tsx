import React, { SetStateAction, useEffect, useState } from 'react';
import './quarkus-project-edition-form.scss';
import { ExtensionEntry, ExtensionsPicker } from '../extensions-picker/extensions-picker';
import { InfoPicker, isValidInfo } from '../info-picker/info-picker';
import { GenerateButton } from '../generate-project/generate-button';
import { Config, QuarkusProject } from '../api/model';
import { debouncedSyncParamsQuery, resolveInitialFilterQueryParam, Target } from '../api/quarkus-project-utils';
import { ExtensionsCart } from '../generate-project/extensions-cart';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  extensions: ExtensionEntry[];
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  config: Config;
  onSave: (target?: Target) => void;
}


export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [ isProjectValid, setIsProjectValid ] = useState(isValidInfo(props.project.metadata));
  const [ filter, setFilter ] = useState(resolveInitialFilterQueryParam());
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
  useEffect(() => {
    debouncedSyncParamsQuery(filter, props.project);
  }, [ filter, props.project ])
  return (
    <div className="quarkus-project-edition-form">
      <div className="form-header-sticky-container">
        <div className="form-header responsive-container">
          <div className="project-info">
            <div className="title">
              <h3>
                Configure your application
              </h3>
            </div>
            <InfoPicker value={props.project.metadata} onChange={setMetadata} quarkusVersion={props.config.quarkusVersion}/>
          </div>
          <div className="generate-project">
            <ExtensionsCart  value={{ extensions: props.project.extensions }} onChange={setExtensions} />
            <GenerateButton project={props.project} generate={save} isProjectValid={isProjectValid} githubClientId={props.config.gitHubClientId}/>
          </div>
        </div>
      </div>
      <div className="project-extensions">
        <div className="title responsive-container" >
          <h3>Extensions</h3>
        </div>
        <ExtensionsPicker
          entries={props.extensions as ExtensionEntry[]}
          value={{ extensions: props.project.extensions }}
          onChange={setExtensions}
          placeholder="Search & Pick extensions: RESTEasy, Hibernate ORM, Web..."
          buildTool={props.project.metadata.buildTool}
          project={props.project}
          filter={filter}
          setFilter={setFilter}
        />
      </div>

    </div>
  );
}