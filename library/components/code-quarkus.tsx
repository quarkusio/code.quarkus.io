import * as React from 'react';
import { Analytics, AnalyticsContext, SegmentAnalyticsImpl, useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import './bootstrap-reboot.css';
import './bootstrap-base.css';
import './code-quarkus.scss';
import { QuarkusProjectFlow } from './quarkus-project/quarkus-project-flow';
import { Config, QuarkusProject } from './api/model';
import { QuarkusBlurb } from './layout/quarkus-blurb';
import { Api, ConfigApi, PlatformApi } from './api/code-quarkus-api';
import { DataLoader, SentryBoundary } from '@quarkusio/code-quarkus.core.components';
import { getQueryParams, resolveInitialFilterQueryParam, resolveInitialProject } from './api/quarkus-project-utils';
import { CodeQuarkusIoHeader } from './header/code-quarkus-io-header';
import { CodeQuarkusHeaderProps } from './header/code-quarkus-header';

export interface ConfiguredCodeQuarkusProps {
  config: Config;
  platformApi: PlatformApi;
  header?: React.FC<CodeQuarkusHeaderProps>;
  api: Api;
}

const queryParams = getQueryParams();

export function ConfiguredCodeQuarkus(props: ConfiguredCodeQuarkusProps) {
  const [ analytics, setAnalytics ] = React.useState<Analytics>(useAnalytics());
  const [ filter, setFilter ] = React.useState(resolveInitialFilterQueryParam());
  const [ project, setProject ] = React.useState<QuarkusProject>(resolveInitialProject(queryParams));

  React.useEffect(() => {
    setAnalytics((prev) => {
      const newAnalytics = props.config.segmentWriteKey ? new SegmentAnalyticsImpl(props.config.segmentWriteKey) : prev;
      newAnalytics.init();
      return newAnalytics;
    });
  }, [ props.config.segmentWriteKey ]);
  const Header: React.FC<CodeQuarkusHeaderProps> = props.header || CodeQuarkusIoHeader;
  const platformLoader = () => props.platformApi(props.api, project.streamKey, project.platformOnly);
  function setStreamKey(streamKey: string, platformOnly: boolean) {
    setProject((prev) => ({ ...prev, streamKey, platformOnly }));
  }
  return (
    <AnalyticsContext.Provider value={analytics}>
      <div className="code-quarkus">
        <DataLoader loader={platformLoader} deps={[ project.streamKey, project.platformOnly ]}>
          {platform => (
            <>
              <Header streamProps={{ platform:platform, streamKey:project.streamKey, setStreamKey, platformOnly: project.platformOnly }} />
              <QuarkusProjectFlow {...props} platform={platform} project={project} setProject={setProject} filter={filter} setFilter={setFilter} />
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
  header?: React.FC<CodeQuarkusHeaderProps>;
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
