
import { StatusMessage } from '@launcher/client';
import { GoogleAnalytics, ProcessingApp, AnalyticsContext, useAnalytics } from '@launcher/component';
import { stringify } from 'querystring';
import React, { useEffect, useState } from 'react';
import { publicUrl } from './config';
import { LauncherQuarkusForm, QuarkusProject } from './form';
import { Header } from './header';
import './launcher-quarkus.scss';
import { NextSteps } from './next-steps';
import { Config } from './config-loader';

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
  config: Config
}

async function generateProject(project: QuarkusProject): Promise<{ downloadLink: string }> {
  const params = {
    g: project.metadata.groupId,
    a: project.metadata.artifactId,
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
  const baseAnalytics = useAnalytics();
  const analytics = props.config.gaTrackingId ? new GoogleAnalytics(props.config.gaTrackingId) : baseAnalytics;
  const progressEvents = run.status === Status.RUNNING && run.result && run.result.events;
  const progressEventsResults = run.status === Status.RUNNING && run.result && run.statusMessages;

  useEffect(() => {
    analytics && analytics.init();
  }, [analytics]);

  const generate = (project: QuarkusProject) => {
    setRun({ status: Status.RUNNING, statusMessages: [] });

    analytics && analytics.event('Flow', 'Generate');
    analytics && project.extensions.forEach(e => analytics.event('Flow', 'Generate-With-Extension', e));

    generateProject(project).then((result) => {
      setRun((prev) => ({ ...prev, result, status: Status.DOWNLOADED }));
    }).catch(error => {
      setRun((prev) => ({ ...prev, status: Status.ERROR, error }));
    });
  };

  return (
    <AnalyticsContext.Provider value={analytics}>
      <div className="launcher-quarkus">
        <Header />
        <LauncherQuarkusForm onSave={project => generate(project)} />
        {run.status === Status.RUNNING && (
          <ProcessingApp progressEvents={progressEvents} progressEventsResults={progressEventsResults} />)}
        {!run.error && run.status === Status.DOWNLOADED
          && (<NextSteps downloadLink={run.result.downloadLink} />)}
      </div>
    </AnalyticsContext.Provider>
  );
}
