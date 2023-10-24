import * as React from 'react';
import { Modal } from 'react-bootstrap';


export function ErrorModal(props: { error: any, onHide: () => void }) {
  return (
    <Modal
      onHide={props.onHide}
      className="error-modal code-quarkus-modal"
      show={true}
      aria-label="Holy Supersonic Atoms..."
    >
      <Modal.Header>Holy Supersonic Atoms!</Modal.Header>
      <Modal.Body>
        <h3>{props.error.message || props.error.toString()}</h3>
      </Modal.Body>
    </Modal>
  );
}
