import React, { useState } from 'react';
import { Button, Text, TextContent, TextVariants } from '@patternfly/react-core';
import { DownloadIcon, CodeIcon } from '@patternfly/react-icons';
import { ExternalLink } from '@launcher/component';
import { FixedModal } from '@launcher/component';

interface NextStepsProps {
  downloadLink?: string;
}

export function NextSteps(props: NextStepsProps) {
  const [open, setOpen] = useState(true);
  const close = () => setOpen(false);
  return (
    <FixedModal
      title="Your new Quarkus app has been generated"
      isOpen={open}
      isLarge={false}
      onClose={close}
      aria-label="Your new Quarkus app has been generated"
      actions={[
        <Button key="launch-new" variant="secondary" aria-label="Create a new Application" onClick={close}>
          Create a new Application
        </Button>,
      ]}
    >
      <TextContent>
        <Text component={TextVariants.h3}>Your new Quarkus app has been generated</Text>
        <Text component={TextVariants.p}>
          Your download should start shortly. If it doesn't, please use the
        </Text>
        <ExternalLink href={props.downloadLink as string} aria-label="Download link">
          <DownloadIcon /> Direct link
        </ExternalLink>
        <Text component={TextVariants.h3}>What's next!</Text>
        <Text component={TextVariants.p}>
        Unzip the project and start playing with Quarkus. You can follow the Quarkus guides to learn more and build a great Quarkus app!
        </Text>
        <ExternalLink href="https://quarkus.io/guides/" aria-label="Start playing with Quarkus">
          <CodeIcon /> Quarkus coding guides
        </ExternalLink>
      </TextContent>
    </FixedModal>
  );
}
