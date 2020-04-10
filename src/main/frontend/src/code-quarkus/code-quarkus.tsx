import { AnalyticsContext, GoogleAnalytics, useAnalytics, Analytics } from '../core';
import { stringify } from 'querystring';
import React, { useEffect, useState } from 'react';
import { publicUrl } from './config';
import { Config } from './config-loader';
import { CodeQuarkusForm } from './form';
import { Header } from './header';
import './code-quarkus.scss';
import { NextSteps } from './next-steps';
import { CLIENT_NAME } from './backend-api';
import { ExtensionEntry } from './pickers/extensions-picker';

enum Status {
  EDITION = 'EDITION', RUNNING = 'RUNNING', ERROR = 'ERROR', DOWNLOADED = 'DOWNLOADED'
}

interface RunState {
  status: Status;
  result?: any;
  error?: any;
}

interface LaunchFlowProps {
  config: Config;
}

export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    packageName?: string;
    buildTool: string;
  };
  extensions: ExtensionEntry[];
}

async function generateProject(environment: string, project: QuarkusProject): Promise<{ downloadLink: string }> {
  const packageName = project.metadata.packageName || project.metadata.groupId;
  const params = {
    ...(project.metadata.groupId && { g: project.metadata.groupId }),
    ...(project.metadata.artifactId && { a: project.metadata.artifactId }),
    ...(project.metadata.version && { v: project.metadata.version }),
    ...(project.metadata.buildTool && { b: project.metadata.buildTool }),
    ...(packageName && { c: `${packageName}.ExampleResource` }),
    ...(project.extensions && { s: project.extensions.map(e => e.shortId).join('.') }),
    cn: CLIENT_NAME,
  };
  const backendUrl = process.env.REACT_APP_BACKEND_URL || publicUrl;
  const downloadLink = `${backendUrl}/d?${stringify(params)}`;
  if (environment !== 'dev') {
    setTimeout(() => window.open(downloadLink, '_blank'), 1000);
  }
  return { downloadLink };
}

const DEFAULT_PROJECT = {
  metadata: {
    groupId: 'org.acme',
    artifactId: 'code-with-quarkus',
    version: '1.0.0-SNAPSHOT',
    buildTool: 'MAVEN'
  },
  extensions: [],
};

export function CodeQuarkus(props: LaunchFlowProps) {
  const [project, setProject] = useState<QuarkusProject>(DEFAULT_PROJECT);
  const [run, setRun] = useState<RunState>({ status: Status.EDITION });
  const [analytics, setAnalytics] = useState<Analytics>(useAnalytics());

  useEffect(() => {
    setAnalytics((prev) => {
      const newAnalytics = props.config.gaTrackingId ? new GoogleAnalytics(props.config.gaTrackingId) : prev;
      newAnalytics.init();
      return newAnalytics;
    });
  }, [props.config.gaTrackingId]);

  const generate = () => {
    setRun({ status: Status.RUNNING });
    analytics.event('UX', 'Generate application', 'Click on "Generate your application" button');
    generateProject(props.config.environment, project).then((result) => {
      setRun((prev) => ({ ...prev, result, status: Status.DOWNLOADED }));
    }).catch(error => {
      setRun((prev) => ({ ...prev, status: Status.ERROR, error }));
    });
  };

  const closeNextSteps = (resetProject = true) => {
    setRun({ status: Status.EDITION });
    if (resetProject) {
      setProject(DEFAULT_PROJECT);
    }
  };

  return (
    <AnalyticsContext.Provider value={analytics}>
      <div className="code-quarkus">
        <Header supportButton={props.config.features && props.config.features.includes('support-button')}/>
        <CodeQuarkusForm project={project} setProject={setProject} onSave={generate} quarkusVersion={props.config.quarkusVersion}/>
        {!run.error && run.status === Status.DOWNLOADED
        && (<NextSteps onClose={closeNextSteps} downloadLink={run.result.downloadLink} buildTool={project.metadata.buildTool} extensions={project.extensions}/>)}
      </div>
    </AnalyticsContext.Provider>
  );
}
