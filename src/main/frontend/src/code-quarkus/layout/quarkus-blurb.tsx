import { Alert, AlertActionCloseButton } from '@patternfly/react-core';
import React from 'react';
import createPersistedState from 'use-persisted-state';
import './quarkus-blurb.scss';
import { useAnalytics } from '../../core';

const useQuarkusBlurbVisibleState = createPersistedState('quarkus-blurb-visible-v1');

export function QuarkusBlurb() {
  const analytics = useAnalytics();
  const [visible, setVisible] = useQuarkusBlurbVisibleState<boolean>(true);
  const close = () => {
    analytics.event('UX', 'Blurb', 'Close');
    setVisible(false);
  };
  const missingFeatureLinkClick = () => {
    analytics.event('UX', 'Blurb', 'Click on "Missing a feature?" link');
  };
  const foundBugLinkClick = () => {
    analytics.event('UX', 'Blurb', 'Click on "Found a bug?" link');
  };
  return (
    <>
      {visible && (
        <Alert className="quarkus-blurb" variant="info" title="This page will help you bootstrap your Quarkus application and discover its extension ecosystem."
               action={<AlertActionCloseButton onClose={close}/>}>
          <p>Think of Quarkus extensions as your project dependencies. Extensions configure, boot and integrate a framework or technology into your Quarkus application. They also do all of the heavy
            lifting of providing the right information to GraalVM for your application to compile natively.</p>
          <br/>
          <p className="desktop-only">Explore the wide breadth of technologies Quarkus applications can be made with.
            The flag <span className="codestart-example-icon" /> means the extension helps you get started with example code.</p>
          <br/>
          <p>Generate your application!</p>
          <br/>
          <p>[<a href="https://github.com/quarkusio/code.quarkus.io/issues/new?labels=feature&template=feature_request.md" onClick={missingFeatureLinkClick} target="_blank" rel="noopener noreferrer">Missing
            a feature?</a> <a href="https://github.com/quarkusio/code.quarkus.io/issues/new?labels=bug&template=bug_report.md" target="_blank" rel="noopener noreferrer" onClick={foundBugLinkClick}>Found
            a bug?</a> We are listening for feedback]</p>
        </Alert>
      )}
      <Alert className="mobile-only quarkus-blurb" variant="info" title="On mobile devices, you can explore the list of Quarkus extensions.">
        <p style={{ color: '#ff004a' }}>If you wish to generate code, try it with your desktop browser...</p>
      </Alert>
    </>
  );
}