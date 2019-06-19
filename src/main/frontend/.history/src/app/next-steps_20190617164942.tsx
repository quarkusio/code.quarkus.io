import { ExternalLink, FixedModal } from '@launcher/component';
import { Button, Text, TextContent, TextVariants } from '@patternfly/react-core';
import React, { useState } from 'react';

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
          Your download should start shortly. If it doesn't, please use the&nbsp;
           <ExternalLink href={props.downloadLink as string} aria-label="Download link">direct link</ExternalLink>
        </Text>
       
        <Text component={TextVariants.h3}>What's next!</Text>
        <Text component={TextVariants.p}>
        Unzip the project and start playing with Quarkus. You can follow the <ExternalLink href="https://quarkus.io/guides/" aria-label="Start playing with Quarkus">Quarkus guides</ExternalLink> 
        <br />to learn more and build a great Quarkus app!
        </Text>
      </TextContent>
    </FixedModal>
  );
}
