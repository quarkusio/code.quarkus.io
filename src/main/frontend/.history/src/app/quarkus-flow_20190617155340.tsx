import React, { useState } from 'react';
import { StatusMessage } from '@launcher/client';
import { useLauncherClient } from '@launcher/component';
import { ProcessingApp } from '@launcher/component';
import { QuarkusForm, QuarkusProject } from './quarkus-form';
import { NextSteps } from './next-steps';

enum Status {
  EDITION = 'EDITION', RUNNING = 'RUNNING', COMPLETED = 'COMPLETED', ERROR = 'ERROR', DOWNLOADED = 'DOWNLOADED'
}

interface RunState {
  status: Status;
  result?: any;
  error?: any;
  statusMessages: StatusMessage[];
}

interface LaunchFlowProps {

}

async function downloadProject(project: QuarkusProject): Promise<{ downloadLink: string }> {
  const params = {
    ...project.metadata,
    extensions
  }
  const link = `/quarkus/download?artifactId=quarkus-project&groupId=com.example&name=quarkus-project&description=My%20project%20with%20Quarkus%0A&packageName=com.example.quarkus-project`;
  window.open(link);
  return {
    downloadLink: 
  };
}


export function QuarkusFlow(props: LaunchFlowProps) {
  const [run, setRun] = useState<RunState>({ status: Status.EDITION, statusMessages: [] });

  const progressEvents = run.status === Status.RUNNING && run.result && run.result.events;
  const progressEventsResults = run.status === Status.RUNNING && run.result && run.statusMessages;

  const download = (project: any) => {
    setRun({ status: Status.RUNNING, statusMessages: [] });

    downloadProject(project).then((result) => {
      setRun((prev) => ({ ...prev, result, status: Status.DOWNLOADED }));
    }).catch(error => {
      setRun((prev) => ({ ...prev, status: Status.ERROR, error }));
    });
  };

  return (
    <React.Fragment>
      <QuarkusForm onSave={project => download(project)} />
      {run.status === Status.RUNNING && (
        <ProcessingApp progressEvents={progressEvents} progressEventsResults={progressEventsResults} />)}
      {!run.error && run.status === Status.DOWNLOADED
        && (<NextSteps downloadLink={run.result.downloadLink} />)}
    </React.Fragment>
  );
}
