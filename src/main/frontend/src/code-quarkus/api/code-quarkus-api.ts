import { Config, Platform } from './model';
import { BACKEND_URL, REQUEST_OPTIONS } from './env';

let platform: Platform | undefined;
let config: Config | undefined;

export async function fetchPlatform() {
  if (platform) {
    return platform!;
  }
  const data = await Promise.all([
    fetch(`${BACKEND_URL}/api/extensions`, REQUEST_OPTIONS)
      .catch(() => Promise.reject(new Error('Fail to fetch the Quarkus extensions list'))),
    fetch(`${BACKEND_URL}/api/streams`, REQUEST_OPTIONS)
      .catch(() => Promise.reject(new Error('Fail to fetch the Quarkus stream list')))
  ]);
  if (!data[0].ok) {
    throw new Error('Failed to load the Quarkus extension list');
  }
  if (!data[1].ok) {
    throw new Error('Failed to load the Quarkus stream list');
  }
  const json = await Promise.all(data.map(d => d.json()));

  return {
    extensions: json[0],
    streams: json[1]
  };
}


export async function fetchConfig() {
  if (config) {
    return config!;
  }
  const data = await fetch(`${BACKEND_URL}/api/config`, REQUEST_OPTIONS)
    .catch(() => Promise.reject(new Error('Fail to fetch the configuration')));
  if (!data.ok) {
    throw new Error('Failed to load Quarkus config');
  }
  config = await data.json();
  return config!;
}