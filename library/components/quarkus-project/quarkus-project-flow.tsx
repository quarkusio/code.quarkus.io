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
import { Platform, QuarkusProject } from '../api/model';
import { Api } from '../api/code-quarkus-api';

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

  return (
    <>
      <CodeQuarkusForm api={props.api} project={props.project} setProject={props.setProject} config={props.config} onSave={generate} platform={props.platform}/>
      {!run.error && run.status === Status.RUNNING && (
        <LoadingModal/>
      )}
      {!run.error && run.status === Status.DOWNLOADED && (
        <NextStepsModal onClose={closeModal} result={run.result} buildTool={props.project.metadata.buildTool} extensions={mapExtensions(props.platform.extensions, props.project.extensions)}/>
      )}
      {run.error && (
        <ErrorModal onHide={() => closeModal(false)} error={run.error}/>
      )}
    </>
  );

}
