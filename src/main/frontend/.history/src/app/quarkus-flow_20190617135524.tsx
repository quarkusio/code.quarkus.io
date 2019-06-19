import React, { useState } from 'react';
import { StatusMessage } from '@launcher/client';
import { useLauncherClient } from '@launcher/component';
import { ProcessingApp } from '@launcher/component';
import { QuarkusForm } from './quarkus-form';
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

export function QuarkusFlow(props: LaunchFlowProps) {
  const [run, setRun] = useState<RunState>({status: Status.EDITION, statusMessages: []});
  const client = useLauncherClient();

  const progressEvents = run.status === Status.RUNNING && run.result && run.result.events;
  const progressEventsResults = run.status === Status.RUNNING && run.result && run.statusMessages;

  const download = (project: any) => {
    setRun({status: Status.RUNNING, statusMessages: []});

    client.download(project).then((result) => {
      setRun((prev) => ({...prev, result, status: Status.DOWNLOADED}));
    }).catch(error => {
      setRun((prev) => ({...prev, status: Status.ERROR, error}));
    });
  };

  return (
    <React.Fragment>
      <QuarkusForm onSave={project => download(project)}/>
      {run.status === Status.RUNNING && (
        <ProcessingApp progressEvents={progressEvents} progressEventsResults={progressEventsResults}/>)}
      {!run.error && run.status === Status.DOWNLOADED
      && (<NextSteps downloadLink={run.result.downloadLink}/>)}
    </React.Fragment>
  );
}
