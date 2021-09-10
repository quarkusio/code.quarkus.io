import React from 'react';
import './stream-picker.scss';
import { Platform, Stream } from '../api/model';
import { normalizeStreamKey } from '../api/quarkus-project-utils';


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
  setStreamKey: (string?)=> void;
}

export function StreamPicker(props: StreamPickerProps) {
  const recommendedStream = getRecommendedStream(props.platform);
  const stream = getProjectStream(props.platform, props.streamKey) || recommendedStream;
  const streamKeys = stream.key.split(':');
  return (
      <div className="current-quarkus-stream" title={`Quarkus core version: ${stream.quarkusCoreVersion}`}>
        <span className="platform-key">{streamKeys[0]}</span>
        <span className="stream-id">{streamKeys[1]}</span>
      </div>
  );
}