import { Modal, TextContent } from '@patternfly/react-core';
import React from 'react';
import { Code } from '../../core';
import { IdeaSupportResult } from '../quarkus-project/quarkus-project-flow';

export function IdeaModal(props: { ideaSupport: IdeaSupportResult, onClose: () => void }) {
  const baseEvent = ['UX', 'Post-Generate Popup Action'];

  return (
    <React.Fragment>
      {props.ideaSupport && props.ideaSupport.isSupported ? (
        <Modal
          title="Your project is openning in IDEA!"
          isSmall={true}
          onClose={props.onClose}
          className="next-steps-modal code-quarkus-modal"
          isOpen={true}
          aria-label="Your project is openning in IDEA!"
        >
          <TextContent>
            <h3>Your Supersonic Subatomic App is openning in your IDEA...</h3>
          </TextContent>
        </Modal>
      ) : (
        <Modal
          title="Holy Atoms! The idea was not found..."
          isSmall={true}
          onClose={props.onClose}
          className="error-modal code-quarkus-modal"
          isOpen={true}
          aria-label="Holy Atoms! The idea was not found..."
        >
          <TextContent>
            <h3>It was not able to open your project in IDEA. You can use git clone</h3>
            <Code event={[...baseEvent, 'Copy git clone command']} content={`git clone ${props.ideaSupport.gitURL}`}/>
          </TextContent>
        </Modal>
      )}
    </React.Fragment>
  );
}