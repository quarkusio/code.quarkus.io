import React, { SetStateAction, useEffect, useState } from 'react';
import './quarkus-project-edition-form.scss';
import { ExtensionEntry, ExtensionsPicker } from '../extensions-picker/extensions-picker';
import { InfoPicker, isValidInfo } from '../info-picker/info-picker';
import { GenerateButton } from '../generate-project/generate-button';
import { Config, Platform, QuarkusProject } from '../api/model';
import {
  debouncedSyncParamsQuery,
  mapExtensions,
  resolveInitialFilterQueryParam,
  Target
} from '../api/quarkus-project-utils';
import { ExtensionsCart } from '../generate-project/extensions-cart';
import { Api } from '../api/code-quarkus-api';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  platform: Platform;
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  config: Config;
  api: Api;
  onSave: (target?: Target) => void;
}

export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [ isProjectValid, setIsProjectValid ] = useState(isValidInfo(props.project.metadata));
  const [ filter, setFilter ] = useState(resolveInitialFilterQueryParam());
  const setProject = props.setProject;

  const selectedExtensions = mapExtensions(props.platform.extensions, props.project.extensions);
  const setMetadata = (metadata: any) => {
    setIsProjectValid(isValidInfo(metadata));
    setProject((prev) => ({ ...prev, metadata }));
  };
  const setExtensions = (value: { extensions: ExtensionEntry[] }) => setProject((prev) => ({ ...prev, extensions: value.extensions.map(e => e.id) }));
  const save = (target?: Target) => {
    if (isProjectValid) {
      props.onSave(target);
    }
  };
  useEffect(() => {
    debouncedSyncParamsQuery(props.api, props.project, filter);
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
            <InfoPicker value={props.project.metadata} onChange={setMetadata} />
          </div>
          <div className="generate-project">
            <ExtensionsCart  value={{ extensions: selectedExtensions }} onChange={setExtensions} tagsDef={props.platform.tagsDef}/>
            <GenerateButton api={props.api} project={props.project} generate={save} isProjectValid={isProjectValid} githubClientId={props.config.gitHubClientId}/>
          </div>
        </div>
      </div>
      <div className="project-extensions">
        <ExtensionsPicker
          entries={props.platform.extensions as ExtensionEntry[]}
          tagsDef={props.platform.tagsDef}
          value={{ extensions: selectedExtensions }}
          onChange={setExtensions}
          placeholder="Search & Pick extensions: jaxrs, hibernate, reactive, web, data..."
          buildTool={props.project.metadata.buildTool}
          project={props.project}
          filter={filter}
          setFilter={setFilter}
        />
      </div>

    </div>
  );
}