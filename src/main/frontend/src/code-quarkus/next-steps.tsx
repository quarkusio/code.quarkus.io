import { ExternalLink } from '../core';
import { Button, Modal, TextContent } from '@patternfly/react-core';
import React from 'react';
import { CopyToClipboard } from './copy-to-clipboard';

interface NextStepsProps {
  downloadLink?: string;
  onClose?(reset?: boolean): void;
  buildTool: string
}

export function NextSteps(props: NextStepsProps) {
  const close = (reset?: boolean) => {
    props.onClose && props.onClose(reset);
  };
  return (
    <Modal
      title="Your Quarkus Application is Ready!"
      isSmall={true}
      className="next-steps-modal"
      onClose={close}
      isOpen={true}
      aria-label="Your new Quarkus app has been generated"
      actions={[
        <Button key="go-back" variant="secondary" aria-label="Go back" onClick={() => close(false)}>
          Go back
        </Button>,
        <Button key="start-new" variant="secondary" aria-label="Start a new application" onClick={() => close()}>
          Start a new application
        </Button>
      ]}
    >
      <TextContent>
        <p>Your download should start shortly. If it doesn't, please use the direct link:</p>
        <Button component="a" href={props.downloadLink as string} aria-label="Download link" className="download-button">Download the zip</Button>
        <h1>What's next?</h1>
        <div>
          Unzip the project and start playing with Quarkus by running:

          {props.buildTool === 'MAVEN' &&
          <code>$ ./mvnw compile quarkus:dev <CopyToClipboard zIndex={5000} tooltipPosition="left" eventId="Cmd.StartDevMode" content="./mvnw compile quarkus:dev"/></code>
          }

          {props.buildTool === 'GRADLE' &&
          <code>$ ./gradlew quarkusDev <CopyToClipboard zIndex={5000} tooltipPosition="left" eventId="Cmd.StartDevMode" content="./gradlew quarkusDev"/></code>
          }
          Follow the <ExternalLink href="https://quarkus.io/guides/" aria-label="Start playing with Quarkus">guides</ExternalLink>  for your next steps!
        </div>
      </TextContent>
    </Modal>
  );
}
