import { Alert, Button, Modal } from 'react-bootstrap';
import React from 'react';
import { Platform, PlatformMappedExtensions, QuarkusProject } from '../api/model';

export function MissingExtensionWarningModal(props: {mappedExtensions: PlatformMappedExtensions; project: QuarkusProject; platform: Platform; setMappingWarning: React.Dispatch<boolean|undefined>}) {
  return (<Modal className="code-quarkus-modal"
    show={true}>
    <Modal.Header>Missing extensions warning</Modal.Header>
    <Modal.Body>
      <Alert variant="warning"><i>{props.mappedExtensions.missing.map(e => `'${e}'`).join(', ')}</i> {props.mappedExtensions.missing.length > 1 ? 'are': 'is'} missing from the current stream <b>'{props.project.streamKey}'</b>, {props.mappedExtensions.missing.length > 1 ? 'they have': 'it has'} been unselected.</Alert>
    </Modal.Body>
    <Modal.Footer>
      <Button key="close" variant="primary" aria-label="Close this popup" onClick={() => props.setMappingWarning(false)}>OK</Button>
    </Modal.Footer>
  </Modal>);
}