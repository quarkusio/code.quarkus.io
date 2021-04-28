import { Loader } from '../../core';
import { Modal } from '@patternfly/react-core';
import React from 'react';


export function LoadingModal(props: {}) {
  return (
    <Modal
      title="Supersonic Subatomic push to GitHub..."
      variant="small"
      className="loading-modal code-quarkus-modal"
      isOpen={true}
      aria-label="Supersonic Subatomic push to GitHub..."
    >
      <Loader/>
    </Modal>
  );
}
