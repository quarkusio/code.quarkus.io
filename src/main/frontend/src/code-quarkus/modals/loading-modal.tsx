import { Loader } from '../../core';
import { Modal } from '@patternfly/react-core';
import React from 'react';


export function LoadingModal(props: {}) {
  return (
    <Modal
      title="Subatomic processing..."
      isSmall={true}
      className="loading-modal code-quarkus-modal"
      isOpen={true}
      aria-label="Subatomic processing..."
    >
      <Loader/>
    </Modal>
  );
}
