import {createGitHubProject} from './code-quarkus-github-api';
import {Extension, PlatformMappedExtensions, QuarkusProject, Platform, Stream } from './model';
import _ from 'lodash';
import {Api} from './code-quarkus-api';


const ERROR_STREAM: Stream = { 
  key: 'recommended.not.found:stream', 
  quarkusCoreVersion: 'error', 
  recommended: true, 
  status: 'NOT_FOUND', 
  platformVersion: 'error',
  lts: false,
  javaCompatibility: { versions: [], recommended: -1 }
}

export function getRecommendedStream(platform: Platform) {
  return platform.streams.find(s => s.recommended) || ERROR_STREAM;
}

export function getProjectStream(platform: Platform, streamKey?: string): Stream {
  const recommendedStream = getRecommendedStream(platform);
  if (!streamKey) {
    return recommendedStream;
  }
  const normalizedStreamKey = normalizeStreamKey(recommendedStream.key.split(':')[0], streamKey);
  return platform.streams.find(s => s.key === normalizedStreamKey) || recommendedStream;
}
export function parse(str): object {
  const decode = decodeURIComponent;
  return (str + '')
    .replace(/\+/g, ' ')
    .split('&')
    .filter(Boolean)
    .reduce(function (obj, item) {
      const ref = item.split('=');
      const key = decode(ref[0] || '');
      const val = decode(ref[1] || '');
      const prev = obj[key];
      obj[key] = prev === undefined ? val : [].concat(prev, val);
      return obj;
    }, {});
}

export function stringify(obj): string {
  const encode = encodeURIComponent;
  return Object.keys(obj || {})
    .reduce(function (arr, key) {
      [].concat(obj[key]).forEach(function (v) {
        arr.push(encode(key) + '=' + encode(v));
      });
      return arr;
    }, [])
    .join('&')
    .replace(/\s/g, '+');
}

export enum Target {
  GENERATE = 'GENERATE',
  DOWNLOAD = 'DOWNLOAD',
  GITHUB = 'GITHUB'
}

export enum LocalStorageKey {
  DEFAULT_PROJECT = 'quarkus:default-project-config'
}

export interface GenerateResult {
  target: Target;
  url: string;
  shareUrl?: string;
}

export interface ProjectPayload {
  groupId?: string;
  artifactId?: string;
  version?: string;
  buildTool?: string;
  javaVersion?: string;
  noCode?: boolean;
  extensions?: string[];
  platformOnly?: boolean;
  streamKey?: string;
}

export function parseQuery(query: string) {
  return parse(query);
}

export function generateProjectPayload(project: QuarkusProject): ProjectPayload {
  const defaultProject = newDefaultProject();
  const changes: ProjectPayload = {};
  if (project.metadata.groupId && project.metadata.groupId !== defaultProject.metadata.groupId) {
    changes.groupId = project.metadata.groupId;
  }
  if (project.metadata.artifactId && project.metadata.artifactId !== defaultProject.metadata.artifactId) {
    changes.artifactId = project.metadata.artifactId;
  }
  if (project.metadata.version && project.metadata.version !== defaultProject.metadata.version) {
    changes.version = project.metadata.version;
  }
  if (project.metadata.buildTool && project.metadata.buildTool !== defaultProject.metadata.buildTool) {
    changes.buildTool = project.metadata.buildTool;
  }
  if (project.metadata.javaVersion && project.metadata.javaVersion !== defaultProject.metadata.javaVersion) {
    changes.javaVersion = project.metadata.javaVersion;
  }
  if (project.metadata.noCode && project.metadata.noCode !== defaultProject.metadata.noCode) {
    changes.noCode = project.metadata.noCode;
  }
  if (project.streamKey) {
    changes.streamKey = project.streamKey;
  }
  if (project.platformOnly !== undefined) {
    changes.platformOnly = project.platformOnly;
  }
  if (project.extensions && project.extensions.length > 0) {
    changes.extensions = project.extensions;
  }
  return changes;
}

const QUERY_PROJECT_MAPPING: any = {
  groupId: 'g',
  artifactId: 'a',
  version: 'v',
  buildTool: 'b',
  javaVersion: 'j',
  noCode: 'nc',
  extensions: 'e',
  streamKey: 'S',
  platformOnly: 'po'
};

export function generateProjectQuery(api: Api, project: QuarkusProject,
  github: boolean = false,
  showClientName: boolean = true): string {
  const payload = generateProjectPayload(project);
  const params: any = {};
  for (const key in QUERY_PROJECT_MAPPING) {
    if (QUERY_PROJECT_MAPPING.hasOwnProperty(key)) {
      const pValue = (payload as any)[key];
      if (pValue !== undefined) {
        if (key === 'extensions') {
          params[QUERY_PROJECT_MAPPING['extensions']] = payload.extensions!!.map(id => toShortcut(id));
        } else {
          params[QUERY_PROJECT_MAPPING[key]] = pValue;
        }
      }
    }
  }
  if (showClientName) {
    params.cn = api.clientName;
  }

  if (github) {
    params.github = true;
  }

  return stringify(params);
}

export function toShortcut(id: string) {
  return id.replace(/^(io.quarkus:quarkus-)|^(quarkus-)/, '');
}

const BASE_LOCATION = window.location.href.replace(window.location.search, '');

export function getProjectDownloadUrl(api: Api, project: QuarkusProject) {
  const baseUrl = api.backendUrl.startsWith('http') ? api.backendUrl : BASE_LOCATION;
  return `${baseUrl.replace(/\/$/, '')}/d?${generateProjectQuery(api, project)}`;
}

export function getProjectShareUrl(api: Api, project: QuarkusProject, github = false) {
  return `${BASE_LOCATION}?${generateProjectQuery(api, project, github, false)}`;
}


export async function generateProject(api: Api, environment: string, project: QuarkusProject, target: Target): Promise<GenerateResult> {
  switch (target) {
  case Target.DOWNLOAD:
  case Target.GENERATE:
    const url = getProjectDownloadUrl(api, project);
    const shareUrl = getProjectShareUrl(api, project);
    if (target === Target.DOWNLOAD ) {
      setTimeout(() => window.open(url, '_blank'), 500);
    }
    return { target, url, shareUrl };
  case Target.GITHUB:
    const result = await createGitHubProject(api, project);
    return { target, url: result.url };
  }
}

export const createOnGitHubUrl = (api: Api, project: QuarkusProject, clientId: string) => {
  const authParams = {
    redirect_uri: getProjectShareUrl(api, project, true),
    client_id: clientId,
    scope: 'public_repo,workflow',
    state: Math.random().toString(36)
  };
  return `https://github.com/login/oauth/authorize?${stringify(authParams)}`;
};


export const createOnGitHub = (api: Api, project: QuarkusProject, clientId: string) => {
  window.location.href = createOnGitHubUrl(api, project, clientId)
};

export function newDefaultProject(): QuarkusProject {
  return ({
    metadata: {
      groupId: 'org.acme',
      artifactId: 'code-with-quarkus',
      version: '1.0.0-SNAPSHOT',
      buildTool: 'MAVEN',
      noCode: false
    },
    extensions: [],
  });
}

const FILTER_PARAM_NAME = 'extension-search';

export function isFilterEmpty(filterParam: string = '') {
  return filterParam.trim() === DEFAULT_FILTER.trim();
}


function syncParamsInQuery(api: Api, project: QuarkusProject | undefined, filterParam: string = ''): void {
  const filter = isFilterEmpty(filterParam) ? '' : filterParam;
  if (!project) {
    window.history.replaceState(null, '', `/?${formatParam(FILTER_PARAM_NAME, filter)}`);
    return;
  }
  window.history.replaceState(null, '', `/${generateParamQuery(formatParam(FILTER_PARAM_NAME, filter), generateProjectQuery(api, project, false, false))}`);
}

export const debouncedSyncParamsQuery = _.debounce(syncParamsInQuery, 500);

const defaultCleanHistory = () => {
  console.log(`remove query from url: ${window.location.search}`);
  window.history.replaceState({}, document.title, window.location.href.replace(window.location.search, ''));
};

export function resolveQueryParams(search: string = window.location.search.substr(1),
  cleanHistory: () => void = defaultCleanHistory): object | undefined {
  if (search.length === 0) {
    return undefined;
  }
  let queryParams = parse(search);
  cleanHistory();
  return queryParams;
}

let queryParams : object | undefined = undefined;

export function normalizeStreamKey(recommendedPlatformId: string, streamKey?: string) {
  if (streamKey == null) {
    return null;
  }
  return streamKey!.indexOf(':') >= 0 ? streamKey! : `${recommendedPlatformId}:${streamKey}`;
}

export function getQueryParams(): object | undefined {
  if (!queryParams) {
    queryParams = resolveQueryParams();
  }
  return queryParams;
}

export const DEFAULT_FILTER = ''

export function resolveInitialFilterQueryParam(queryParams = getQueryParams()): string {
  if (!queryParams || !queryParams[FILTER_PARAM_NAME]) {
    return DEFAULT_FILTER;
  }

  return queryParams![FILTER_PARAM_NAME]!.toString();
}

const formatParam = (paramName: string, value: string): string => {
  if (value) {
    return `${paramName}=${value}`;
  }

  return '';
};

const generateParamQuery = (filter: string, project: string) => {
  if (filter && project) {
    return `?${project}&${filter}`;
  }

  if (project) {
    return `?${project}`;
  }

  if (filter) {
    return `?${filter}`;
  }

  return '';
};

export function resolveInitialProject(queryParams?: object) {
  return parseProjectInQuery(queryParams) || retrieveProjectFromLocalStorage() || newDefaultProject();
}

function normalizeQueryExtensions(queryExtensions: undefined | string | string[]): Set<string> {
  return new Set((queryExtensions ? Array.isArray(queryExtensions) ? queryExtensions : [ queryExtensions ] : [])
    .map(e => toShortcut(e)));
}

export function mapExtensions(catalog: Extension[], extensions: string[]): PlatformMappedExtensions {
  const selected = extensions.map(e => toShortcut(e));
  const catalogMap = new Map(catalog.map(e => [ toShortcut(e.id), e ]));
  const missing = extensions.filter(id => !catalogMap.has(toShortcut(id)))
  let mapped = selected.filter(id => catalogMap.has(id)).map(id => catalogMap.get(id));
  return  {
    mapped,
    missing
  };
}

export function parseProjectInQuery(queryParams?: object): QuarkusProject | undefined {
  if (!queryParams) {
    return undefined;
  }
  const queryExtensions = normalizeQueryExtensions(queryParams['e']);
  const defaultProj = newDefaultProject();
  const project = {
    metadata: {
      groupId: queryParams['g'] || defaultProj.metadata.groupId,
      artifactId: queryParams['a'] || defaultProj.metadata.artifactId,
      version: queryParams['v'] || defaultProj.metadata.version,
      buildTool: queryParams['b'] || defaultProj.metadata.buildTool,
      javaVersion: queryParams['j'] || defaultProj.metadata.javaVersion,
      noCode: queryParams['nc'] || defaultProj.metadata.noCode
    },
    extensions: Array.from(queryExtensions),
    streamKey: queryParams['S'],
    platformOnly: queryParams['po'] || defaultProj.platformOnly,
    github: queryParams['github'] === 'true' ? {
      code: queryParams['code'],
      state: queryParams['state']
    } : undefined
  } as QuarkusProject;
  if (project.github) {
    console.log('Received GitHub auth');
  }
  return project;
}

export function retrieveProjectFromLocalStorage() : QuarkusProject | undefined {
  try {
    const jsonProject = localStorage.getItem(LocalStorageKey.DEFAULT_PROJECT);

    if(!jsonProject) {
      return undefined;
    }

    const payload = JSON.parse(jsonProject!) as ProjectPayload;

    const project = newDefaultProject();
    if(payload.groupId) {
      project.metadata.groupId = payload.groupId;
    }
    if(payload.artifactId) {
      project.metadata.artifactId = payload.artifactId;
    }
    if(payload.version) {
      project.metadata.version = payload.version;
    }
    if(payload.buildTool) {
      project.metadata.buildTool = payload.buildTool;
    }
    if(payload.javaVersion) {
      project.metadata.javaVersion = payload.javaVersion;
    }
    if(payload.noCode) {
      project.metadata.noCode = payload.noCode;
    }
    if(payload.extensions) {
      project.extensions = payload.extensions;
    }
    return project;
  } catch (err) {
    return undefined;
  }
}

export function saveProjectToLocalStorage(project : QuarkusProject) {
  const projectPayload = generateProjectPayload(project);
  projectPayload.streamKey = null;
  const jsonProject = JSON.stringify(projectPayload);
  localStorage.setItem(LocalStorageKey.DEFAULT_PROJECT, jsonProject);
}

export function resetProjectToDefault() {
  localStorage.removeItem(LocalStorageKey.DEFAULT_PROJECT);
}

export function existsStoredProject(): boolean {
  const jsonProject = localStorage.getItem(LocalStorageKey.DEFAULT_PROJECT);
  return jsonProject !== null;
}
