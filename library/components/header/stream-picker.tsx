import React from 'react';
import './stream-picker.scss';
import { Platform, Stream } from '../api/model';
import { getRecommendedStream, getProjectStream } from '../api/quarkus-project-utils';
import { Dropdown } from 'react-bootstrap';
import { FaAngleDown, FaCheck } from 'react-icons/fa';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import classNames from 'classnames';



function formatStreamStatus(status?: string, quarkusCoreVersion?: string) {
  let s = status?.toLowerCase();
  if (s === 'final') {
    return null;
  }
  return  s;
}

function parseStreamKey(key: string) {
  const streamKey = key.split(':');
  const platformKey = streamKey[0];
  const streamId = streamKey[1];

  return {
    platformKey, streamId
  }
}

export interface StreamPickerProps {
  platform: Platform;
  streamKey?: string;
  platformOnly?: boolean;
  setStreamKey: (string?, boolean?) => void;
}

const SelectedStream = (props: {stream: Stream}) => {
  const platformVersion = props.stream.platformVersion;
  const recommended = props.stream.recommended;
  
  const status = formatStreamStatus(props.stream.status, props.stream.quarkusCoreVersion);
  const { platformKey, streamId} = parseStreamKey(props.stream.key);
  
  return (
    <div className={classNames('quarkus-stream', status)} title={platformVersion}>
      <span className="stream-id">
        {streamId}
        { (status && !recommended) && <span className="stream-status">{status}</span> }
      </span>
      <span className="platform-key">{platformKey}</span>
    </div>
  );
}

function StreamItem(props: { streamKey: string; quarkusCoreVersion?: string; platformVersion?: string; recommended: boolean; selected?: boolean; status?: string }) {
  const status = formatStreamStatus(props.status, props.quarkusCoreVersion);
  const { platformKey, streamId} = parseStreamKey(props.streamKey);
  
  return (
    <div className={classNames('quarkus-stream', status)} title={props.platformVersion}>
      {props.selected ? <span className="selected"><FaCheck /></span> : <span className="unselected"/>}
      <span className="platform-key">{platformKey}</span>
      <span className="stream-id">{streamId}</span>
      {props.recommended && <span className="tag recommended">(recommended)</span>}
      {status && <span className="tag status">({status})</span>}
    </div>
  );
}

export function StreamPicker(props: StreamPickerProps) {
  const analytics = useAnalytics();
  const recommendedStream = getRecommendedStream(props.platform);
  const stream = getProjectStream(props.platform, props.streamKey);
  function setStreamKey(s: Stream) {
    props.setStreamKey(s.key, props.platformOnly);
    analytics.event('Switch stream', { stream: s.key, element: 'stream-picker' });
  }

  return (
    <>
      <Dropdown className="stream-picker">
        <Dropdown.Toggle className="current-stream" as="div">
          <SelectedStream stream={stream}/>
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