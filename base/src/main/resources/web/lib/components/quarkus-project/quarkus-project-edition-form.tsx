import * as React from 'react';
import './quarkus-project-edition-form.scss';
import { ExtensionEntry, ExtensionsPicker } from '../extensions-picker/extensions-picker';
import { getProjectStream } from '../api/quarkus-project-utils';
import { InfoPicker, isValidInfo } from '../info-picker/info-picker';
import { GenerateButton } from '../generate-project/generate-button';
import { Config, Extension, Platform, QuarkusProject } from '../api/model';
import {
  debouncedSyncParamsQuery,
  Target,
  saveProjectToLocalStorage,
  resetProjectToDefault
} from '../api/quarkus-project-utils';
import { ExtensionsCart } from '../generate-project/extensions-cart';
import { Api } from '../api/code-quarkus-api';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  selectedExtensions: Extension[];
  platform: Platform;
  setProject: React.Dispatch<React.SetStateAction<QuarkusProject>>;
  filter: string;
  setFilter: React.Dispatch<React.SetStateAction<string>>;
  config: Config;
  api: Api;
  onSave: (target?: Target) => void;
}

export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [ isProjectValid, setIsProjectValid ] = React.useState(isValidInfo(props.project.metadata));
  const { setProject, filter, setFilter } = props;
  const [ isConfigSaved, setConfigSaved ] = React.useState(false);

  const setMetadata = (metadata: any) => {
    setIsProjectValid(isValidInfo(metadata));
    setProject((prev) => ({ ...prev, metadata }));
    setConfigSaved(false);
  };
  const setExtensions = (value: { extensions: ExtensionEntry[] }) => {
    setProject((prev) => ({ ...prev, extensions: value.extensions.map(e => e.id) }));
    setConfigSaved(false);
  }

  const save = (target?: Target) => {
    if (isProjectValid) {
      props.onSave(target);
    }
  };

  const storeAppConfig = () => {
    if(isProjectValid) {
      saveProjectToLocalStorage(props.project);
      setConfigSaved(true);
    }
  };

  const resetAppConfig = () => {
    resetProjectToDefault();
    setConfigSaved(false);
  };

  React.useEffect(() => {
    debouncedSyncParamsQuery(props.api, props.project, filter);
  }, [ filter, props.project ]);
  
  const currentStream = getProjectStream(props.platform, props.project.streamKey);
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
            <InfoPicker currentStream={currentStream} value={props.project.metadata} onChange={setMetadata} />
          </div>
          <div className="generate-project">
            <ExtensionsCart  value={{ extensions: props.selectedExtensions }} onChange={setExtensions} tagsDef={props.platform.tagsDef}/>
            <GenerateButton 
              api={props.api} 
              project={props.project} 
              generate={save} 
              isProjectValid={isProjectValid} 
              githubClientId={props.config.gitHubClientId}
              isConfigSaved={isConfigSaved}
              storeAppConfig={storeAppConfig}
              resetAppConfig={resetAppConfig}
            />
          </div>
        </div>
      </div>
      <div className="project-extensions">
        <ExtensionsPicker
          entries={props.platform.extensions as ExtensionEntry[]}
          tagsDef={props.platform.tagsDef}
          value={{ extensions: props.selectedExtensions }}
          onChange={setExtensions}
          placeholder="Filter & Pick extensions: jaxrs, hibernate, reactive, web, data..."
          buildTool={props.project.metadata.buildTool}
          project={props.project}
          filter={filter}
          setFilter={setFilter}
        />
      </div>

    </div>
  );
}