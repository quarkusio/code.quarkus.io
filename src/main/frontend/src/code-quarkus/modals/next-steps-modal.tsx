import { CopyToClipboard, createLinkTracker, ExternalLink, useAnalytics } from '../../core';
import React from 'react';
import { GenerateResult, Target } from '../api/quarkus-project-utils';
import { Extension } from '../api/model';
import { Button, Modal } from 'react-bootstrap';

interface NextStepsProps {
  result: GenerateResult;
  buildTool: string;
  extensions: Extension[];

  onClose?(reset?: boolean): void;
}

export function NextStepsModal(props: NextStepsProps) {
  const analytics = useAnalytics();
  const baseEvent = [ 'UX', 'Post-Generate Popup Action' ];
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
  const devModeEvent = [ ...baseEvent, 'Copy "Dev mode" command' ];
  const zip = props.result.target === Target.DOWNLOAD || props.result.target === Target.GENERATE;
  return (
    <Modal
      className="next-steps-modal code-quarkus-modal"
      onHide={() => close(false)}
      show={true}
      aria-label="Your new Quarkus app has been generated"
    >
      <Modal.Header><h2>Your Supersonic Subatomic App is ready!</h2></Modal.Header>
      <Modal.Body>
        {zip && (
          <React.Fragment>
            {props.result.target === Target.DOWNLOAD ? (
              <p>Your download should start shortly. If it doesn't, please use the direct link:</p>
            ) : (
              <p>It's time to download it:</p>
            )}
            <Button as="a" href={props.result.url} aria-label="Download the zip" className="download-button"
              onClick={linkTracker}>Download the zip</Button>
          </React.Fragment>
        )}
        {props.result.target === Target.GITHUB && (
          <React.Fragment>
            <p>Your application is now on <ExternalLink href={props.result.url} aria-label={'Open GitHub repository'} onClick={linkTracker}>GitHub</ExternalLink> ready to be cloned:</p>
            <CopyToClipboard className="code" id="copy-git-clone-cmd-code" light={true} event={[ ...baseEvent, 'Copy git clone command' ]} content={`git clone ${props.result.url}`} zIndex={1100}>
              <code className="code">git clone {props.result.url}</code>
            </CopyToClipboard>
          </React.Fragment>
        )}

        <h3>What's next?</h3>
        <div>
          {zip && (
            <p>Unzip the project and start playing with Quarkus :)</p>
          )}
          {props.result.target === Target.GITHUB && (
            <p>Once your project is cloned locally, start playing with Quarkus;</p>
          )}

          <p>with the <ExternalLink href="https://quarkus.io/guides/cli-tooling" aria-label={'Open Quarkus CLI guide'} onClick={linkTracker}>Quarkus CLI</ExternalLink>:</p>

          <CopyToClipboard className="code" id="copy-cli-cmd-code" light={true}  event={devModeEvent} content=".quarkusDev" zIndex={1100} tooltipPlacement="top">
            <code className="code">quarkus dev</code>
          </CopyToClipboard>

          <p>with your favorite build tool:</p>
          {props.buildTool === 'MAVEN' && (
            <CopyToClipboard className="code" id="copy-mvn-cmd-code" light={true} event={devModeEvent} content="./mvnw compile quarkus:dev" zIndex={1100} tooltipPlacement="top">
              <code className="code">./mvnw compile quarkus:dev</code>
            </CopyToClipboard>
          )}

          {props.buildTool.startsWith('GRADLE')  && (
            <CopyToClipboard className="code" id="copy-gradle-cmd-code" light={true}  event={devModeEvent} content="./gradlew quarkusDev" zIndex={1100} tooltipPlacement="top">
              <code className="code">./gradlew quarkusDev</code>
            </CopyToClipboard>
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
      </Modal.Body>
      <Modal.Footer>
        <Button key="go-back" variant="secondary" aria-label="Close this popup" onClick={() => close(false)}>
          Close
        </Button>
        <Button key="start-new" variant="secondary" aria-label="Start a new application" onClick={() => close()}>
          Start a new application
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
