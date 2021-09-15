import React, { SetStateAction, useEffect, useState } from 'react';
import {
  generateProject,
  newDefaultProject,
  Target,
  mapExtensions
} from '../api/quarkus-project-utils';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { CodeQuarkusForm } from './quarkus-project-edition-form';
import { LoadingModal } from '../modals/loading-modal';
import { NextStepsModal } from '../modals/next-steps-modal';
import { ConfiguredCodeQuarkusProps } from '../code-quarkus';
import { ErrorModal } from '../modals/error-modal';
import { Platform, QuarkusProject, Tag } from '../api/model';
import { Api } from '../api/code-quarkus-api';
import { Alert, Button, Modal } from 'react-bootstrap';
import { MissingExtensionWarningModal } from '../layout/missing-extension-warning-modal';

enum Status {
  EDITION = 'EDITION', RUNNING = 'RUNNING', ERROR = 'ERROR', DOWNLOADED = 'DOWNLOADED'
}

interface RunState {
  status: Status;
  result?: any;
  error?: any;
}

interface QuarkusProjectFlowProps extends ConfiguredCodeQuarkusProps {
  platform: Platform;
  api: Api;
  project: QuarkusProject;
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
}

export function QuarkusProjectFlow(props: QuarkusProjectFlowProps) {
  const [ run, setRun ] = useState<RunState>({ status: Status.EDITION });
  const [ mappingWarning, setMappingWarning ] = useState<boolean | undefined>(undefined);
  const analytics = useAnalytics();

  const generate = (target: Target = Target.DOWNLOAD) => {
    if (run.status !== Status.EDITION) {
      console.error(`Trying to generate an application from the wrong status: ${run.status}`);
      return;
    }
    if (target === Target.GITHUB) {
      setRun({ status: Status.RUNNING });
    }
    analytics.event('UX', 'Generate application', target);
    generateProject(props.api, props.config.environment, props.project, target).then((result) => {
      setRun((prev) => ({ ...prev, result, status: Status.DOWNLOADED }));
    }).catch((error: any) => {
      setRun((prev) => ({ status: Status.ERROR, error }));
    });
    if (target === Target.GITHUB) {
      props.setProject(prev => ({ ...prev, github: undefined }));
    }
  };

  useEffect(() => {
    if (props.project.github) {
      generate(Target.GITHUB);
    }
    // eslint-disable-next-line
  }, []);

  const closeModal = (resetProject = true) => {
    setRun({ status: Status.EDITION });
    if (resetProject) {
      props.setProject(newDefaultProject());
    }
  };

  const mappedExtensions = mapExtensions(props.platform.extensions, props.project.extensions);
  
  useEffect(() => {
    if(mappingWarning === undefined && mappedExtensions.missing.length > 0) {
      setMappingWarning(true);
      props.setProject((prev) => ({ ...prev, extensions: mappedExtensions.mapped.map(e => e.id) }));
    }
  }, [ mappingWarning, mappedExtensions.missing ]);
  
  return (
    <>
      <CodeQuarkusForm api={props.api} project={props.project} setProject={props.setProject} config={props.config} onSave={generate} platform={props.platform} selectedExtensions={mappedExtensions.mapped}/>
      {!run.error && run.status === Status.RUNNING && (
        <LoadingModal/>
      )}
      {!run.error && run.status === Status.DOWNLOADED && (
        <NextStepsModal onClose={closeModal} result={run.result} buildTool={props.project.metadata.buildTool} extensions={mappedExtensions.mapped}/>
      )}
      {run.error && (
        <ErrorModal onHide={() => closeModal(false)} error={run.error}/>
      )}
      {!!mappingWarning && <MissingExtensionWarningModal mappedExtensions={mappedExtensions} platform={props.platform} setMappingWarning={setMappingWarning} project={props.project} />}
    </>
  );

}
