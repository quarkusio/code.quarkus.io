import { Alert, AlertActionCloseButton } from '@patternfly/react-core';
import React from "react";
import createPersistedState from 'use-persisted-state';
import './quarkus-blurb.scss';

const useQuarkusBlurbVisibleState = createPersistedState('quarkus-blurb-visible-v1');

export function QuarkusBlurb() {
  const [visible, setVisible] = useQuarkusBlurbVisibleState<Boolean>(true);
  return (
    <>
      {visible && (
        <Alert className="quarkus-blurb" variant="info" title="This page will help you bootstrap your Quarkus application and discover its extension ecosystem." action={<AlertActionCloseButton onClose={() => setVisible(false)} />}>
          <p>Think of Quarkus extensions as your project dependencies. Extensions configure, boot and integrate a framework or technology into your Quarkus application. They also do all of the heavy lifting of providing the right information to GraalVM for your application to compile natively.</p>
          <br />
          <p className="desktop-only">Explore the wide breadth of technologies Quarkus applications can be made with. Generate your application!</p>
          <p className="mobile-only" style={{ color: "#ff004a" }}>On mobile devices, you can explore the list of Quarkus extensions.<br /> If you wish to generate code, try it with your desktop browser...</p>
          <br />
          <p>[<a href="https://github.com/quarkusio/code.quarkus.io/issues/new" target="_blank" rel="noopener noreferrer">Missing a feature?</a> <a href="https://github.com/quarkusio/code.quarkus.io/issues/new" target="_blank" rel="noopener noreferrer">Found a bug?</a> We are listening for feedbacks]</p>
        </Alert>)
      }
    </>
  );
}