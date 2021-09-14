import React, { useState } from 'react';
import './stream-picker.scss';
import { Platform, Stream } from '../api/model';
import { normalizeStreamKey } from '../api/quarkus-project-utils';
import { Alert, Button, Dropdown, Form, Modal, Overlay } from 'react-bootstrap';
import { FaAngleDown, FaAngleRight, FaCheck, FaStar } from 'react-icons/fa';
import { HoverControlledDropdown } from '@quarkusio/code-quarkus.core.components';
import classNames from 'classnames';


const ERROR_STREAM: Stream = { key: 'recommended.not.found:stream', quarkusCoreVersion: 'error', recommended: true }

function getRecommendedStream(platform: Platform) {
  return platform.streams.find(s => s.recommended) || ERROR_STREAM;
}

function getProjectStream(platform: Platform, streamKey?: string) {
  if (!streamKey) {
    return null;
  }
  const recommendedStream = getRecommendedStream(platform);
  const normalizedStreamKey = normalizeStreamKey(recommendedStream.key.split(':')[0], streamKey);
  return platform.streams.find(s => s.key == normalizedStreamKey);
}

export interface StreamPickerProps {
  platform: Platform;
  streamKey?: string;
  setStreamKey: (string?) => void;
  hasSelectedExtension: boolean;
}

function StreamItem(props: { streamKey: string; quarkusCoreVersion: string; recommended: boolean; selected?: boolean; }) {
  const streamKeys = props.streamKey.split(':');
  return (
    <div className="quarkus-stream" title={`Quarkus core version: ${props.quarkusCoreVersion}`}>
      {props.selected && <span className="selected"><FaCheck /></span>}
      <span className="platform-key">{streamKeys[0]}</span>
      <span className="stream-id">{streamKeys[1]}</span>
      {props.recommended && <span className="recommended">(recommended)</span>}
    </div>
  );
}

export function StreamPicker(props: StreamPickerProps) {
  const [ switchStream, setSwitchStream ] = useState<Stream | undefined>();
  const recommendedStream = getRecommendedStream(props.platform);
  const stream = getProjectStream(props.platform, props.streamKey) || recommendedStream;
  function showWarning(s: Stream) {
    if(props.hasSelectedExtension) {
      setSwitchStream(s);
    } else {
      props.setStreamKey(s.key);
    }
  }
  function confirmSwitch() {
    if(switchStream) {
      props.setStreamKey(switchStream!.key);
      setSwitchStream(undefined);
    }
  }
  return (
    <>
      {!!switchStream && <Modal className="code-quarkus-modal"
        show={true}>
        <Modal.Header>Do you want to change the Quarkus Stream</Modal.Header>
        <Modal.Body>
          <Alert variant="info">When changing stream, the list of extension could change, some extension might get unselected.</Alert>
        </Modal.Body>
        <Modal.Footer>
          <Button key="close" variant="secondary" aria-label="Close this popup" onClick={() => setSwitchStream(undefined)}>No</Button>
          <Button key="close" variant="primary" aria-label="No" onClick={() => confirmSwitch()}>Yes</Button>
        </Modal.Footer>
      </Modal>}
      <Dropdown
        placement="right"
        overlay={Overlay}
        delay={{ show: 200, hide: 0 }}
        className="stream-picker">
        <Dropdown.Toggle className="current-stream" as="div">
          <StreamItem streamKey={stream.key} quarkusCoreVersion={stream.quarkusCoreVersion} recommended={false}/>
          { props.platform.streams.length > 1 && <FaAngleDown />}
        </Dropdown.Toggle>
        <Dropdown.Menu>
          {props.platform.streams.map((s, i) => (
            <Dropdown.Item as="div" key={i} onClick={() => s !== stream && showWarning(s)}>
              <StreamItem streamKey={s.key} quarkusCoreVersion={s.quarkusCoreVersion} recommended={s.recommended} selected={s === stream}/>
            </Dropdown.Item>
          ))}
        </Dropdown.Menu>
      </Dropdown>
    </>
  );
}