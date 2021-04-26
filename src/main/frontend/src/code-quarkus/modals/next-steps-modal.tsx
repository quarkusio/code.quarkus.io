import { Code, createLinkTracker, ExternalLink, useAnalytics } from '../../core';
import { Button, Modal, TextContent } from '@patternfly/react-core';
import React from 'react';
import { GenerateResult, Target } from '../api/quarkus-project-utils';
import { Extension } from '../api/model';

interface NextStepsProps {
  result: GenerateResult;
  buildTool: string;
  extensions: Extension[];

  onClose?(reset?: boolean): void;
}

export function NextStepsModal(props: NextStepsProps) {
  const analytics = useAnalytics();
  const baseEvent = ['UX', 'Post-Generate Popup Action'];
  const close = (reset?: boolean) => {
    analytics.event(baseEvent[0], baseEvent[1], reset ? 'Start new' : 'Close');
    if (props.onClose) props.onClose(reset);
  };
  const linkTracker = createLinkTracker(analytics, baseEvent[0], baseEvent[1], 'aria-label');
  const onClickGuide = (id: string) => (e: any) => {
    linkTracker(e);
    analytics.event('Extension', 'Click "Open Extension Guide" link', id);
  };
  const extensionsWithGuides = props.extensions.filter(e => !!e.guide);
  const devModeEvent = [...baseEvent, 'Copy "Dev mode" command'];
  return (
    <Modal
      title="Your Supersonic Subatomic App is ready!"
      isSmall={true}
      className="next-steps-modal code-quarkus-modal"
      onClose={() => close(false)}
      isOpen={true}
      aria-label="Your new Quarkus app has been generated"
      actions={[
        (
          <Button key="go-back" variant="secondary" aria-label="Close this popup" onClick={() => close(false)}>
            Close
          </Button>
        ),
        (
          <Button key="start-new" variant="secondary" aria-label="Start a new application" onClick={() => close()}>
            Start a new application
          </Button>
        )
      ]}
    >
      <TextContent>
        {props.result.target === Target.SHARE && (
          <React.Fragment>
            <p>Your can share the link to the zip:</p>
            <Code event={[...baseEvent, 'Copy the download link']} content={`${props.result.url}`}/>
            <p>Or the link to this configured application:</p>
            <Code event={[...baseEvent, 'Copy the share link']} content={`${props.result.shareUrl}`}/>
          </React.Fragment>
        )}
        {props.result.target === Target.DOWNLOAD && (
          <React.Fragment>
            <p>Your download should start shortly. If it doesn't, please use the direct link:</p>
            <Button component="a" href={props.result.url} aria-label="Download the zip" className="download-button"
                    onClick={linkTracker}>Download the zip</Button>
          </React.Fragment>
        )}
        {props.result.target === Target.GITHUB && (
          <React.Fragment>
            <p>Your application is now on <ExternalLink href={props.result.url} aria-label={`Open GitHub repository`} onClick={linkTracker}>GitHub</ExternalLink> ready to be cloned:</p>
            <Code event={[...baseEvent, 'Copy git clone command']} content={`git clone ${props.result.url}`}/>
          </React.Fragment>
        )}

        <h1>What's next?</h1>
        <div>
          {props.result.target === Target.DOWNLOAD && (
            <p>Unzip the project and start playing with Quarkus by running:</p>
          )}
          {props.result.target === Target.GITHUB && (
            <p>Once your project is cloned locally, start playing with Quarkus by running:</p>
          )}

          {props.buildTool === 'MAVEN' && (
            <Code event={devModeEvent} content="./mvnw compile quarkus:dev"/>
          )}

          {props.buildTool.startsWith('GRADLE')  && (
            <Code event={devModeEvent} content="./gradlew quarkusDev"/>
          )}
        </div>
        {extensionsWithGuides.length === 1 && (
          <div>
            <b>Follow the <ExternalLink href={extensionsWithGuides[0].guide!} aria-label={`Open ${extensionsWithGuides[0].name} guide`} onClick={onClickGuide(extensionsWithGuides[0].id)}>{extensionsWithGuides[0].name} guide</ExternalLink> for your next steps!</b>
          </div>
        )}
        {extensionsWithGuides.length > 1 && (
          <div>
            <b>Follow the guides we prepared for your application:</b>
            <ul>
              {extensionsWithGuides.map((e, i) => (
                <li key={i}>
                  <ExternalLink href={e.guide!} aria-label="Start playing with Quarkus" onClick={onClickGuide(e.id)}>{e.name}</ExternalLink>
                </li>
              ))}
            </ul>
          </div>
        )}
        <div>
          <br/>
          For more fun, have a look to our various <ExternalLink href="https://quarkus.io/guides/" aria-label="Open guides" onClick={linkTracker}>Quarkus guides</ExternalLink>...
        </div>
      </TextContent>
    </Modal>
  );
}
