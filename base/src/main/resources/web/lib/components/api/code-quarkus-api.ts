import { Config, Platform, Tag } from './model';
import _ from "lodash";

let platformCache: Map<string, Platform> = new Map<string, Platform>();
let config: Config | undefined;

export interface Api {
  backendUrl: string;
  clientName: string;
  tagsDef?: Tag[];
  requestOptions: RequestInit;
}

export type PlatformApi = (api: Api, streamKey?: string, platformOnly?: boolean) => Promise<Platform>
export type ConfigApi = (api: Api) => Promise<Config>

export const DEFAULT_TAGS: Tag[] = [
  {
    name: 'status:preview',
    background: '#1f6feb',
    color: '#ffffff',
    description: 'This is work in progress. API or configuration properties might change as the extension matures. Give us your feedback :)'
  },
  {
    name: 'status:experimental',
    background: '#d73a49',
    color: '#ffffff',
    description: 'Early feedback is requested to mature the idea. There is no guarantee of stability nor long term presence in the platform until the solution matures.'
  },
  {
    name: 'status:deprecated',
    background: '#6a737d',
    color: '#ffffff',
    description: 'This extension has been deprecated. It is likely to be replaced or removed in a future version of Quarkus'
  },
  {
    name: 'with:starter-code',
    border: '#ffdd57',
    description: 'This extension provides starter code (may not be available in all languages).'
  },
  {
    name: 'status:stable',
    hide: true
  },
  {
    name: 'origin:platform',
    hide: true
  },
  {
    name: 'origin:other',
    hide: true
  }
];

export async function fetchPlatform(api: Api, streamKey?: string, platformOnly: boolean = false) {
  const cacheKey = `${streamKey || 'recommended'}-${String(platformOnly)}`;
  if (platformCache.has(cacheKey)) {
    return platformCache.get(cacheKey);
  }
  const extensionsPath = streamKey ? `/extensions/stream/${streamKey}?platformOnly=${String(platformOnly)}` : `/extensions?platformOnly=${String(platformOnly)}`;
  const presetsPath = streamKey ? `/presets/stream/${streamKey}` : `/presets`;
  const data = await Promise.all([
    fetch(`${api.backendUrl}${extensionsPath}`, api.requestOptions)
      .catch(() => Promise.reject(new Error('Failed to fetch the Quarkus extensions list from the api'))),
    fetch(`${api.backendUrl}/streams`, api.requestOptions)
      .catch(() => Promise.reject(new Error('Failed to fetch the Quarkus stream list from the api'))),
    fetch(`${api.backendUrl}${presetsPath}`, api.requestOptions)
      .catch(() => Promise.reject(new Error('Failed to fetch the Quarkus extensions list from the api')))
  ]);
  if (!data[0].ok) {
    throw new Error('Failed to load the Quarkus extension list');
  }
  if (!data[1].ok) {
    throw new Error('Failed to load the Quarkus stream list');
  }
  const json = await Promise.all(data.map(d => d.json()));

  let platform = {
    extensions: json[0],
    extensionById: _.keyBy(json[0], ({id}) => id),
    streams: json[1],
    presets: json[2],
    tagsDef: api.tagsDef || DEFAULT_TAGS
  };
  platformCache.set(cacheKey, platform);
  return platform;
}

export async function fetchConfig(api: Api) {
  if (config) {
    return config!;
  }
  const data = await fetch(`${api.backendUrl}/config`, api.requestOptions)
    .catch(() => Promise.reject(new Error('Failed to fetch the configuration from the api')));
  if (!data.ok) {
    throw new Error('Failed to load Quarkus config');
  }
  config = await data.json();
  return config!;
}