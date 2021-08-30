import { parse, ParsedUrlQuery, stringify } from 'querystring';
import { createGitHubProject } from './code-quarkus-github-api';
import { Extension, QuarkusProject } from './model';
import _ from 'lodash';
import { Api } from './code-quarkus-api';

export enum Target {
  GENERATE = 'GENERATE',
  DOWNLOAD = 'DOWNLOAD',
  GITHUB = 'GITHUB'
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
  noCode?: boolean;
  extensions?: string[];
  streamKey?: string;
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
  if (project.metadata.noCode && project.metadata.noCode !== defaultProject.metadata.noCode) {
    changes.noCode = project.metadata.noCode;
  }
  if (project.streamKey) {
    changes.streamKey = project.streamKey;
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
  noCode: 'nc',
  extensions: 'e',
  streamKey: 'S'
};

export function generateProjectQuery(api: Api, project: QuarkusProject,
  github: boolean = false,
  showClientName: boolean = true): string {
  const payload = generateProjectPayload(project);
  const params: any = {};
  for (const key in QUERY_PROJECT_MAPPING) {
    if (QUERY_PROJECT_MAPPING.hasOwnProperty(key)) {
      const pValue = (payload as any)[key];
      if (pValue) {
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

export const createOnGitHub = (api: Api, project: QuarkusProject, clientId: string) => {
  const authParams = {
    redirect_uri: getProjectShareUrl(api, project, true),
    client_id: clientId,
    scope: 'public_repo,workflow',
    state: Math.random().toString(36)
  };
  const githubAuthorizeUrl = `https://github.com/login/oauth/authorize?${stringify(authParams)}`;
  window.location.href = githubAuthorizeUrl;
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

function syncParamsInQuery(api: Api, project: QuarkusProject | undefined, filterParam: string = ''): void {
  if (!project) {
    console.log(filterParam);
    window.history.replaceState(null, '', `/?${formatParam(FILTER_PARAM_NAME, filterParam)}`);
    return;
  }
  window.history.replaceState(null, '', `/${generateParamQuery(formatParam(FILTER_PARAM_NAME, filterParam), generateProjectQuery(api, project, false, false))}`);
}

export const debouncedSyncParamsQuery = _.debounce(syncParamsInQuery, 500);

const defaultCleanHistory = () => {
  console.log(`remove query from url: ${window.location.search}`);
  window.history.replaceState({}, document.title, window.location.href.replace(window.location.search, ''));
};

export function resolveQueryParams(search: string = window.location.search.substr(1),
  cleanHistory: () => void = defaultCleanHistory): ParsedUrlQuery | undefined {
  if (search.length === 0) {
    return undefined;
  }
  let queryParams = parse(search);
  cleanHistory();
  return queryParams;
}

let queryParams: ParsedUrlQuery | undefined = undefined;

export function normalizeStreamKey(recommendedPlatformId: string, streamKey?: string) {
  if (streamKey == null) {
    return null;
  }
  return streamKey!.indexOf(':') >= 0 ? streamKey! : `${recommendedPlatformId}:${streamKey}`;
}

export function getQueryParams(): ParsedUrlQuery | undefined {
  if (!queryParams) {
    queryParams = resolveQueryParams();
  }
  return queryParams;
}


export function resolveInitialFilterQueryParam(queryParams = getQueryParams()): string {
  if (!queryParams || !queryParams[FILTER_PARAM_NAME]) {
    return '';
  }

  return queryParams![FILTER_PARAM_NAME]!.toString() || '';
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

export function resolveInitialProject(queryParams?: ParsedUrlQuery) {
  return parseProjectInQuery(queryParams) || newDefaultProject();
}

function normalizeQueryExtensions(queryExtensions: undefined | string | string[]): Set<string> {
  return new Set((queryExtensions ? Array.isArray(queryExtensions) ? queryExtensions : [ queryExtensions ] : [])
    .map(e => toShortcut(e)));
}

export function mapExtensions(catalog: Extension[], extensions: string[]): Extension[] {
  const eSet = new Set(extensions.map(e => toShortcut(e)));
  return  _.uniqBy(catalog.filter(e => eSet.has(toShortcut(e.id))), e => e.id);
}

export function parseProjectInQuery(queryParams?: ParsedUrlQuery): QuarkusProject | undefined {
  if (!queryParams) {
    return undefined;
  }
  const queryExtensions = normalizeQueryExtensions(queryParams?.e);
  const defaultProj = newDefaultProject();
  const project = {
    metadata: {
      groupId: queryParams.g || defaultProj.metadata.groupId,
      artifactId: queryParams.a || defaultProj.metadata.artifactId,
      version: queryParams.v || defaultProj.metadata.version,
      buildTool: queryParams.b || defaultProj.metadata.buildTool,
      noCode: queryParams.nc || defaultProj.metadata.noCode
    },
    extensions: Array.from(queryExtensions),
    streamKey: queryParams.S,
    github: queryParams.github === 'true' ? {
      code: queryParams.code,
      state: queryParams.state
    } : undefined
  } as QuarkusProject;
  if (project.github) {
    console.log('Received GitHub auth');
  }
  return project;
}