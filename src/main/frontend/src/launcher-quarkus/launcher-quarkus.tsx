
import React, { useState } from 'react';
import { StatusMessage } from '@launcher/client';
import { ProcessingApp } from '@launcher/component';
import { stringify } from 'querystring';
import { publicUrl } from './config';
import './launcher-quarkus.scss';
import { NextSteps } from './next-steps';
import { Header } from './header';
import { QuarkusProject, LauncherQuarkusForm } from './form';


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
    g: project.metadata.groupId,
    a: project.metadata.artifactId,
    d: project.metadata.description,
    v: project.metadata.version,
    c: `${project.metadata.packageName}.QuarkusApp`,
    e: project.extensions
  }
  const downloadLink = `${publicUrl}/api/quarkus/download?${stringify(params)}`;
  window.open(downloadLink, '_blank');
  return { downloadLink };
}

export function LauncherQuarkus(props: LaunchFlowProps) {
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
    <div className="launcher-quarkus">
      <Header />
      <LauncherQuarkusForm onSave={project => download(project)} />
      {run.status === Status.RUNNING && (
        <ProcessingApp progressEvents={progressEvents} progressEventsResults={progressEventsResults} />)}
      {!run.error && run.status === Status.DOWNLOADED
        && (<NextSteps downloadLink={run.result.downloadLink} />)}
    </div>
  );
}
