import { Modal, TextContent } from '@patternfly/react-core';
import React from 'react';


export function ErrorModal(props: { error: any, onClose: () => void }) {
  return (
    <Modal
      title="Holy Supersonic Atoms!"
      variant="small"
      onClose={props.onClose}
      className="error-modal code-quarkus-modal"
      isOpen={true}
      aria-label="Holy Supersonic Atoms..."
    >
      <TextContent>
        <h3>{props.error.message || props.error.toString()}</h3>
      </TextContent>
    </Modal>
  );
}
