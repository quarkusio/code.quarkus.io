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
      aria-label="Your Application has been generated"
      actions={[
        <Button key="launch-new" variant="secondary" aria-label="Start a new Application" onClick={close}>
          Start a new Application
        </Button>,
      ]}
    >
      <TextContent>
        <Text component={TextVariants.h3}>Download your application</Text>
        <Text component={TextVariants.p}>
         Your download should start shortly. If it doesn't, please use the
        </Text>
        <ExternalLink href={props.downloadLink as string} aria-label="Download link">
          <DownloadIcon/> Direct link
        </ExternalLink>
        <Text component={TextVariants.h3}>Deploy it on OpenShift</Text>
        <Text component={TextVariants.p}>
          Your new application contains a tool to help you deploy your new application on OpenShift.<br/>
          You can find instructions in the README.md.
        </Text>
        <Text component={TextVariants.h3}>As soon as deployment is done, go checkout your new application capabilities</Text>
        <Text component={TextVariants.p}>We prepared a set of examples to let you directly start playing with your new application.<br/>
          Those examples are there to get you started,<br/>
          soon it will be time for you to remove them and start developing your awesome application.</Text>
      </TextContent>
    </FixedModal>
  );
}
