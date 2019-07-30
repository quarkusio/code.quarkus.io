import { ExternalLink } from '@launcher/component';
import { Button, Modal, Text, TextContent, TextVariants } from '@patternfly/react-core';
import React from 'react';
import { CopyToClipboard } from './copy-to-clipboard';

interface NextStepsProps {
  downloadLink?: string;
  onClose?(reset?: boolean): void;
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
        <Button key="launch-new" variant="secondary" aria-label=" Go back" onClick={() => close(false)}>
          Go back
        </Button>,
        <Button key="launch-new" variant="secondary" aria-label=" Start Over" onClick={() => close()}>
          Start Over
        </Button>
      ]}
    >
      <TextContent>
        <Text component={TextVariants.p}>Your download should start shortly. If it doesn't, please use the direct link:</Text>
        <Button component="a" href={props.downloadLink as string} aria-label="Download link" className="download-button">Download the zip</Button>
        <Text component={TextVariants.h1}>What's next!</Text>
        <Text component={TextVariants.p}>
          Unzip the project and start playing with Quarkus by running:
        <code>$ ./mvnw compile quarkus:dev <CopyToClipboard content="./mvnw compile quarkus:dev" /></code>
          Follow the <ExternalLink href="https://quarkus.io/guides/" aria-label="Start playing with Quarkus">guides</ExternalLink>  for your next steps!
        </Text>
      </TextContent>
    </Modal>
  );
}
