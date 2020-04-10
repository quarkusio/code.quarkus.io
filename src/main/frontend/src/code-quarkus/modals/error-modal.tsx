import { Modal, TextContent } from '@patternfly/react-core';
import React from 'react';


export function ErrorModal(props: { error: any, onClose: () => void }) {
  return (
    <Modal
      title="Holy guacamole!"
      isSmall={true}
      onClose={props.onClose}
      className="error-modal code-quarkus-modal"
      isOpen={true}
      aria-label="We are preparing your Quarkus Application..."
    >
      <TextContent>
        <h3>Something went wrong while processing your Quarkus App...</h3>
        <p>{props.error.message || props.error.toString()}</p>
      </TextContent>
    </Modal>
  );
}
