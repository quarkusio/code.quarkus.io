import React, { useEffect, useState } from 'react';
import { Analytics, AnalyticsContext, GoogleAnalytics, useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import './bootstrap-reboot.css';
import './bootstrap-base.css';
import './code-quarkus.scss';
import { QuarkusProjectFlow } from './quarkus-project/quarkus-project-flow';
import { Config, QuarkusProject } from './api/model';
import { QuarkusBlurb } from './layout/quarkus-blurb';
import { Api, ConfigApi, PlatformApi } from './api/code-quarkus-api';
import { DataLoader, SentryBoundary } from '@quarkusio/code-quarkus.core.components';
import { getQueryParams, resolveInitialProject } from './api/quarkus-project-utils';
import { CodeQuarkusIoHeader } from './header/code-quarkus-io-header';
import { HeaderProps } from './header/header-props';


export interface ConfiguredCodeQuarkusProps {
  config: Config;
  platformApi: PlatformApi;
  header?: React.FC<HeaderProps>;
  api: Api;
}

const queryParams = getQueryParams();

export function ConfiguredCodeQuarkus(props: ConfiguredCodeQuarkusProps) {
  const [ analytics, setAnalytics ] = useState<Analytics>(useAnalytics());
  const [ project, setProject ] = useState<QuarkusProject>(resolveInitialProject(queryParams));

  useEffect(() => {
    setAnalytics((prev) => {
      const newAnalytics = props.config.gaTrackingId ? new GoogleAnalytics(props.config.gaTrackingId) : prev;
      newAnalytics.init();
      return newAnalytics;
    });
  }, [ props.config.gaTrackingId ]);
  const Header: React.FC<HeaderProps> = props.header || CodeQuarkusIoHeader;
  const platformLoader = () => props.platformApi(props.api, project.streamKey);
  return (
    <AnalyticsContext.Provider value={analytics}>
      <div className="code-quarkus">
        <DataLoader loader={platformLoader} deps={[ project.streamKey ]}>
          {platform => (
            <>
              <Header platform={platform} project={project} />
              <QuarkusProjectFlow {...props} platform={platform} project={project} setProject={setProject} />
            </>
          )}
        </DataLoader>
        <QuarkusBlurb/>
      </div>
    </AnalyticsContext.Provider>
  );
}

export interface CodeQuarkusProps {
  configApi: ConfigApi;
  platformApi: PlatformApi;
  api: Api;
  header?: React.FC<HeaderProps>;
}

export function CodeQuarkus(props: CodeQuarkusProps) {
  const configLoader = () => props.configApi(props.api);
  return (
    <DataLoader loader={configLoader}>{config => (
      <SentryBoundary sentryDSN={config.sentryDSN} environment={config.environment}>
        <ConfiguredCodeQuarkus api={props.api} config={config} platformApi={props.platformApi} header={props.header}/>
      </SentryBoundary>
    )}</DataLoader>
  );
}
