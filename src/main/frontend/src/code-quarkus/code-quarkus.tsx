import { Analytics, AnalyticsContext, GoogleAnalytics, useAnalytics } from '../core';
import React, { useEffect, useState } from 'react';
import { Header } from './layout/header';
import './bootstrap-base.css';
import './code-quarkus.scss';
import { ExtensionsLoader } from './loaders/extensions-loader';
import { QuarkusProjectFlow } from './quarkus-project/quarkus-project-flow';
import { Config } from './api/model';
import { QuarkusBlurb } from './layout/quarkus-blurb';


export interface CodeQuarkusProps {
  config: Config;
}

export function CodeQuarkus(props: CodeQuarkusProps) {
  const [ analytics, setAnalytics ] = useState<Analytics>(useAnalytics());

  useEffect(() => {
    setAnalytics((prev) => {
      const newAnalytics = props.config.gaTrackingId ? new GoogleAnalytics(props.config.gaTrackingId) : prev;
      newAnalytics.init();
      return newAnalytics;
    });
  }, [ props.config.gaTrackingId ]);

  return (
    <AnalyticsContext.Provider value={analytics}>
      <div className="code-quarkus">
        <Header quarkusVersion={props.config.quarkusVersion} supportButton={props.config.features && props.config.features.includes('support-button')}/>
        <ExtensionsLoader name="extensions">
          {extensions => (
            <QuarkusProjectFlow {...props} extensions={extensions}/>
          )}
        </ExtensionsLoader>
        <QuarkusBlurb />
      </div>
    </AnalyticsContext.Provider>
  );
}
