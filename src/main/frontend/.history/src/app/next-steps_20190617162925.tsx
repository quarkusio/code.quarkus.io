import React, { useState } from 'react';
import { Button, Text, TextContent, TextVariants } from '@patternfly/react-core';
import { DownloadIcon } from '@patternfly/react-icons';
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
      title="What's next!"
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
          <DownloadIcon/> Direct link
        </ExternalLink>
        <Text component={TextVariants.h3}>Unzip the project and start playing with Quarkus</Text>
        <Text component={TextVariants.p}>
          Your new application contains a tool to help you deploy your new application on OpenShift.<br/>
          You can find instructions in the README.md.
        </Text>
        <ExternalLink href="https://quarkus.io/guides/getting-started-guide#the-jax-rs-resources" aria-label="Start playing with Quarkus">
          <DownloadIcon/> Direct link
        </ExternalLink>
    </FixedModal>
  );
}
