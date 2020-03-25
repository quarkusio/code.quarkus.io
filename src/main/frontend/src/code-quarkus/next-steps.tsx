import { ExternalLink, useAnalytics } from '../core';
import { Button, Modal, TextContent } from '@patternfly/react-core';
import React from 'react';
import { CopyToClipboard } from './copy-to-clipboard';
import {ExtensionEntry} from "./pickers/extensions-picker";

interface NextStepsProps {
  downloadLink?: string;
  onClose?(reset?: boolean): void;
  buildTool: string
  extensions: ExtensionEntry[];
}

export function NextSteps(props: NextStepsProps) {
  const analytics = useAnalytics();
  const baseEvent = ["UX", "Post-Generate Popup Action"];
  const close = (reset?: boolean) => {
    analytics.event(baseEvent[0], baseEvent[1], reset ? "Start new" : "Close");
    props.onClose && props.onClose(reset);
  };
  const onClickDownload = () => {
    analytics.event(baseEvent[0], baseEvent[1], 'Click "Download the zip" link')
  };
  const onClickGuides = () => {
    analytics.event(baseEvent[0], baseEvent[1], 'Click "guides" link')
  };

  const onClickGuide = (id: string) => () => {
    analytics.event(baseEvent[0], baseEvent[1], 'Click "guide" link');
    analytics.event('Extensions', 'Click "Open Extension Guide" link', id);
  };
  const extensionsWithGuides = props.extensions.filter(e => !!e.guide);
  const devModeEvent = [...baseEvent, 'Copy "Dev mode" command'];
  return (
    <Modal
      title="Your Quarkus Application is Ready!"
      isSmall={true}
      className="next-steps-modal"
      onClose={() => close(false)}
      isOpen={true}
      aria-label="Your new Quarkus app has been generated"
      actions={[
        <Button key="go-back" variant="secondary" aria-label="Close this popup" onClick={() => close(false)}>
          Close
        </Button>,
        <Button key="start-new" variant="secondary" aria-label="Start a new application" onClick={() => close()}>
          Start a new application
        </Button>
      ]}
    >
      <TextContent>
        <p>Your download should start shortly. If it doesn't, please use the direct link:</p>
        <Button component="a" href={props.downloadLink as string} aria-label="Download link" className="download-button" onClick={onClickDownload}>Download the zip</Button>
        <h1>What's next?</h1>
        <div>
          Unzip the project and start playing with Quarkus by running:

          {props.buildTool === 'MAVEN' &&
          <code>$ ./mvnw compile quarkus:dev <CopyToClipboard zIndex={5000} tooltipPosition="left" event={devModeEvent} content="./mvnw compile quarkus:dev"/></code>
          }

          {props.buildTool === 'GRADLE' &&
          <code>$ ./gradlew quarkusDev <CopyToClipboard zIndex={5000} tooltipPosition="left" event={devModeEvent} content="./gradlew quarkusDev"/></code>
          }
        </div>
        {extensionsWithGuides.length === 1 && <div>
          <b>Follow the <ExternalLink href={extensionsWithGuides[0].guide!} aria-label={`${extensionsWithGuides[0].name} guide`} onClick={onClickGuide(extensionsWithGuides[0].id)}>{extensionsWithGuides[0].name} guide</ExternalLink> for your next steps!</b>
        </div>
        }
        {extensionsWithGuides.length > 1 && <div>
          <b>Follow the guides we prepared for your application:</b>
            <ul>
              {extensionsWithGuides.map((e, i) => <li key={i}>
                <ExternalLink href={e.guide!} aria-label="Start playing with Quarkus" onClick={onClickGuide(e.id)}>{e.name}</ExternalLink>
              </li>)}
            </ul>
          </div>
        }
        <div>
          <br />
          For more fun, have a look to our various <ExternalLink href="https://quarkus.io/guides/" aria-label="Start playing with Quarkus" onClick={onClickGuides}>Quarkus guides</ExternalLink>...
        </div>
      </TextContent>
    </Modal>
  );
}
