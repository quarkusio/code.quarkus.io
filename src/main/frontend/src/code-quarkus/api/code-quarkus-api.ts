import { Config, Extension } from './model';
import { BACKEND_URL, REQUEST_OPTIONS } from './env';

let extensions: Extension[] | undefined;
let config: Config | undefined;

export async function fetchExtensions() {
  if (extensions) {
    return extensions!;
  }
  const data = await fetch(`${BACKEND_URL}/api/extensions`, REQUEST_OPTIONS)
    .catch(() => Promise.reject(new Error('Fail to fetch the Quarkus extensions list')));
  if (!data.ok) {
    throw new Error('Failed to load the Quarkus extension list');
  }
  extensions = await data.json();
  return extensions!;
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