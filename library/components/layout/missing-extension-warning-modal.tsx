import { Alert, Button, Modal } from 'react-bootstrap';
import React from 'react';
import { Platform, QuarkusProject } from '../api/model';

export function MissingExtensionWarningModal(props: { missingExtensions: string[]; project: QuarkusProject; platform: Platform; setMissingExtensions: React.Dispatch<string[]>}) {
  return (<Modal className="code-quarkus-modal"
    show={true}>
    <Modal.Header>Missing extensions warning</Modal.Header>
    <Modal.Body>
      <Alert variant="warning"><i>{props.missingExtensions.map(e => `'${e}'`).join(', ')}</i> {props.missingExtensions.length > 1 ? 'are': 'is'} missing from the current stream <b>'{props.project.streamKey}'</b>, {props.missingExtensions.length > 1 ? 'they have': 'it has'} been unselected.</Alert>
    </Modal.Body>
    <Modal.Footer>
      <Button key="close" variant="primary" aria-label="Close this popup" onClick={() => props.setMissingExtensions([])}>OK</Button>
    </Modal.Footer>
  </Modal>);
}