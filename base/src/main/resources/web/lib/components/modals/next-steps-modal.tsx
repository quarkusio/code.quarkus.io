import * as React from 'react';
import { useAnalytics, createLinkTracker } from '../../core/analytics';
import { CopyToClipboard, ExternalLink } from '../../core/components';
import {GenerateResult, Target, createOnGitHub} from '../api/quarkus-project-utils';
import {Extension, QuarkusProject} from '../api/model';
import { Button, Modal } from 'react-bootstrap';
import { FaGithub, FaFileArchive } from 'react-icons/fa';
import {Api} from "../api/code-quarkus-api";

interface NextStepsProps {
  result: GenerateResult;
  buildTool: string;
  extensions: Extension[];
  api: Api;
  project: QuarkusProject;
  githubClientId?: string;
  onClose?(reset?: boolean): void;
}

export function NextStepsModal(props: NextStepsProps) {
  const analytics = useAnalytics();
  const context = { element: 'next-step-modal' };
  const close = (reset?: boolean) => {
    analytics.event('Click', { label: reset ? 'Start new' : 'Close', ...context } );
    if (props.onClose) props.onClose(reset);
  };
  const linkTracker = createLinkTracker(analytics, 'aria-label', context);
  const onClickGuide = (id: string) => (e: any) => {
    linkTracker(e);
    analytics.event('Click', { label: 'Extension guide', extension: id, ...context });
  };
  const extensionsWithGuides = props.extensions.filter(e => !!e.guide);
  const devModeEventContext = { ...context, label: 'Dev mode command' }
  const zip = props.result.target === Target.DOWNLOAD || props.result.target === Target.GENERATE;

  const githubClick = (e: any) => {
    linkTracker(e);
    createOnGitHub(props.api, props.project, props.githubClientId!);
  };

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
            <>
              {props.result.target === Target.DOWNLOAD ? (
                  <p>Your download should start shortly. If it doesn't, please use the direct link:</p>
              ) : (
                  <p>Your download link is ready:</p>
              )}
              <Button as="a" href={props.result.url} aria-label="Download the zip" className="action-button"
                      onClick={linkTracker}><FaFileArchive /><span>Download the zip</span></Button>
              {props.githubClientId && (
                  <>
                    <p>If you want to start collaborating:</p>
                    <Button as="button" aria-label="Create on GitHub" className="action-button github"
                            onClick={githubClick}><FaGithub/><span>Push to GitHub</span></Button>
                  </>
              )}
            </>
        )}
        {props.result.target === Target.GITHUB && (
          <>
            <p>Your application is now on <ExternalLink href={props.result.url} aria-label={'Open GitHub repository'} onClick={linkTracker}>GitHub</ExternalLink> ready to be cloned:</p>
            <CopyToClipboard className="code" id="copy-git-clone-cmd-code" light={true} eventContext={{...context, label: 'git clone command' }} content={`git clone ${props.result.url}`} zIndex={1100}>
              <code className="code">git clone {props.result.url}</code>
            </CopyToClipboard>
          </>
        )}

        <h3>What's next?</h3>
        <div>
          {zip && (
            <p>Unzip the project and start playing with Quarkus :)</p>
          )}
          {props.result.target === Target.GITHUB && (
            <p>Once your project is cloned locally, start playing with Quarkus :)</p>
          )}

          <p>Use the <ExternalLink href="https://quarkus.io/guides/cli-tooling" aria-label={'Open Quarkus CLI guide'} onClick={linkTracker}>Quarkus CLI</ExternalLink>:</p>

          <CopyToClipboard className="code" id="copy-cli-cmd-code" light={true}  eventContext={devModeEventContext} content="quarkus dev" zIndex={1100} tooltipPlacement="top">
            <code className="code">quarkus dev</code>
          </CopyToClipboard>

          <p>Use your favorite build tool:</p>
          {props.buildTool === 'MAVEN' && (
            <CopyToClipboard className="code" id="copy-mvn-cmd-code" light={true} eventContext={devModeEventContext} content="./mvnw compile quarkus:dev" zIndex={1100} tooltipPlacement="top">
              <code className="code">./mvnw compile quarkus:dev</code>
            </CopyToClipboard>
          )}

          {props.buildTool.startsWith('GRADLE')  && (
            <CopyToClipboard className="code" id="copy-gradle-cmd-code" light={true}  eventContext={devModeEventContext} content="./gradlew quarkusDev" zIndex={1100} tooltipPlacement="top">
              <code className="code">./gradlew quarkusDev</code>
            </CopyToClipboard>
          )}

        </div>
        {extensionsWithGuides.length === 1 && (
            <div>
              <b>Follow the <ExternalLink href={extensionsWithGuides[0].guide!}
                                          aria-label={`Open ${extensionsWithGuides[0].name} guide`} onClick={onClickGuide(extensionsWithGuides[0].id)}>{extensionsWithGuides[0].name} guide</ExternalLink> for your next steps!</b>
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
