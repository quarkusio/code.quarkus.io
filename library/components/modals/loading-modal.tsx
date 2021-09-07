import React from 'react';
import { Loader } from '@quarkusio/code-quarkus.core.components';
import { Modal } from 'react-bootstrap';


export function LoadingModal(props: {}) {
  return (
    <Modal
      className="loading-modal code-quarkus-modal"
      show={true}
      aria-label="Supersonic Subatomic push to GitHub..."
    >
      <Modal.Header>
        Supersonic Subatomic push to GitHub..
      </Modal.Header>
      <Modal.Body>
        <Loader/>
      </Modal.Body>
    </Modal>
  );
}
