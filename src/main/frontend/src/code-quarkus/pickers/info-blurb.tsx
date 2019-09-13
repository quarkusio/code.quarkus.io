import { Alert, AlertActionCloseButton } from '@patternfly/react-core';
import React from "react";
import { useSessionStorageWithObject } from 'react-use-sessionstorage';

export function InfoBlurb() {
  const [visible, setVisible] = useSessionStorageWithObject<Boolean>('quarkus-blurb-visible', true);
  return (
    <>
      {visible && (
        <Alert className="quarkus-blurb" variant="info" title="This page will help you bootstrap your Quarkus application and discover its extension ecosystem." action={<AlertActionCloseButton onClose={() => setVisible(false)} />}>
          <p>Think of Quarkus extensions as your project dependencies. Extensions configure, boot and integrate a framework or technology into your Quarkus application. They also do all of the heavy lifting of providing the right information to GraalVM for your application to compile natively.</p>
          <br />
          <p className="desktop-only">Explore the wide breadth of technologies Quarkus applications can be made with. Generate your application!</p>
          <p className="mobile-only" style={{ color: "#ff004a" }}>On mobile devices, you can explore the list of Quarkus extensions.<br /> If you wish to generate code, try it with your desktop browser...</p>
        </Alert>)
      }
    </>
  );
}