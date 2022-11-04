import React from 'react';
import './stream-picker.scss';
import { Platform, Stream } from '../api/model';
import { normalizeStreamKey } from '../api/quarkus-project-utils';
import { Dropdown } from 'react-bootstrap';
import { FaAngleDown, FaCheck } from 'react-icons/fa';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import classNames from 'classnames';


const ERROR_STREAM: Stream = { key: 'recommended.not.found:stream', quarkusCoreVersion: 'error', recommended: true, status: 'NOT_FOUND', platformVersion: 'error' }

function getRecommendedStream(platform: Platform) {
  return platform.streams.find(s => s.recommended) || ERROR_STREAM;
}

function getProjectStream(platform: Platform, streamKey?: string) {
  if (!streamKey) {
    return null;
  }
  const recommendedStream = getRecommendedStream(platform);
  const normalizedStreamKey = normalizeStreamKey(recommendedStream.key.split(':')[0], streamKey);
  return platform.streams.find(s => s.key === normalizedStreamKey);
}

export interface StreamPickerProps {
  platform: Platform;
  streamKey?: string;
  platformOnly?: boolean;
  setStreamKey: (string?, boolean?) => void;
}

function StreamItem(props: { streamKey: string; quarkusCoreVersion?: string; platformVersion?: string; recommended: boolean; selected?: boolean; status?: string }) {
  const streamKeys = props.streamKey.split(':');
  let status = props.status?.toLowerCase();
  if(!status) {
    status = props.quarkusCoreVersion?.toLowerCase().indexOf('final') >= 0  ? 'final' : 'cr';
  }
  return (
    <div className={classNames('quarkus-stream', status)} title={props.platformVersion}>
      {props.selected ? <span className="selected"><FaCheck /></span> : <span className="unselected"/>}
      <span className="platform-key">{streamKeys[0]}</span>
      <span className="stream-id">
        {streamKeys[1]}
        { (status !== 'final' && !props.recommended) && <span className="stream-status">{status}</span> }
      </span>
      {props.recommended && <span className="tag recommended">(recommended)</span>}
      {status !== 'final'  && <span className="tag status">({status})</span>}
    </div>
  );
}

export function StreamPicker(props: StreamPickerProps) {
  const analytics = useAnalytics();
  const recommendedStream = getRecommendedStream(props.platform);
  const stream = getProjectStream(props.platform, props.streamKey) || recommendedStream;
  function setStreamKey(s: Stream) {
    props.setStreamKey(s.key, props.platformOnly);
    analytics.event('UX', 'Stream Picker', s.key);
  }

  return (
    <>
      <Dropdown className="stream-picker">
        <Dropdown.Toggle className="current-stream" as="div">
          <StreamItem streamKey={stream.key} quarkusCoreVersion={stream.quarkusCoreVersion} platformVersion={stream.platformVersion} recommended={false} status={stream.status}/>
          { props.platform.streams.length > 1 && <FaAngleDown />}
        </Dropdown.Toggle>
        <Dropdown.Menu>
          {props.platform.streams.map((s, i) => (
            <Dropdown.Item as="div" key={i} onClick={() => s !== stream && setStreamKey(s)}>
              <StreamItem streamKey={s.key} quarkusCoreVersion={s.quarkusCoreVersion} platformVersion={s.platformVersion} recommended={s.recommended} selected={s === stream} status={s.status}/>
            </Dropdown.Item>
          ))}
        </Dropdown.Menu>
      </Dropdown>
    </>
  );
}