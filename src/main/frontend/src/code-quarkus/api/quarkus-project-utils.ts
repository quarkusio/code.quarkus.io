import { parse, ParsedUrlQuery, stringify } from 'querystring';
import { ExtensionEntry } from '../pickers/extensions-picker';
import { createGitHubProject } from './code-quarkus-github-api';
import { QuarkusProject } from './model';
import _ from 'lodash';
import { BACKEND_URL, CLIENT_NAME } from './env';

export enum Target {
  DOWNLOAD = 'DOWNLOAD',
  SHARE = 'SHARE',
  GITHUB = 'GITHUB'
}

export interface GenerateResult {
  target: Target;
  url: string;
  shareUrl?: string;
}

export function generateProjectQuery(project: QuarkusProject,
                                     github: boolean = false,
                                     showClientName: boolean = true): string {
  const defaultProject = newDefaultProject();

  const params: any = {
    ...(project.metadata.groupId &&  project.metadata.groupId !== defaultProject.metadata.groupId) && { g: project.metadata.groupId },
    ...(project.metadata.artifactId &&  project.metadata.artifactId !== defaultProject.metadata.artifactId) && { a: project.metadata.artifactId },
    ...(project.metadata.version && project.metadata.version !== defaultProject.metadata.version) && { v: project.metadata.version },
    ...(project.metadata.buildTool && project.metadata.buildTool !== defaultProject.metadata.buildTool) && { b: project.metadata.buildTool },
    ...(project.metadata.noExamples && project.metadata.noExamples !== defaultProject.metadata.noExamples) && { ne: project.metadata.noExamples },
    ...(project.extensions && project.extensions.length !== defaultProject.extensions.length) && { e: project.extensions.map(e => toShortcut(e.id)) },
    ...(showClientName && { cn: CLIENT_NAME })
  };
  if (github) {
    params.github = true;
  }

  return stringify(params);
}

export function toShortcut(id: string) {
  return id.replace(/^(io.quarkus:quarkus-)|^(quarkus-)/, '');
}

const BASE_LOCATION = window.location.href.replace(window.location.search, '');

export function getProjectDownloadUrl(project: QuarkusProject) {
  const baseUrl = BACKEND_URL.startsWith('http') ? BACKEND_URL : BASE_LOCATION;
  return `${baseUrl.replace(/\/$/, '')}/d?${generateProjectQuery(project)}`;
}

export function getProjectShareUrl(project: QuarkusProject, github = false) {
  return `${BASE_LOCATION}?${generateProjectQuery(project, github, false)}`;
}

export async function generateProject(environment: string, project: QuarkusProject, target: Target): Promise<GenerateResult> {
  switch (target) {
    case Target.DOWNLOAD:
    case Target.SHARE:
      const url = getProjectDownloadUrl(project);
      const shareUrl = getProjectShareUrl(project);
      if (target !== Target.SHARE && environment !== 'dev') {
        setTimeout(() => window.open(url, '_blank'), 500);
      }
      return { target, url, shareUrl };
    case Target.GITHUB:
      const result = await createGitHubProject(project);
      return { target, url: result.url };
  }
}

export const createOnGitHub = (project: QuarkusProject, clientId: string) => {
  const authParams = {
    redirect_uri: getProjectShareUrl(project, true),
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
      noExamples: false
    },
    extensions: [],
  });
}

const queryName = 'extension-search';

export function resolveInitialFilterQueryParam(queryParams?: ParsedUrlQuery): string {
  if(!queryParams || !queryParams[queryName]) {
    return '';
  }

  return queryParams[queryName].toString() || '';
}


function syncParamsInQuery(filterParam: string = '', project: QuarkusProject | undefined): void {
  if (!project) {
    window.history.replaceState(null, '', `/?${formatParam(queryName, filterParam)}`);
    return;
  }

  window.history.replaceState(null, '', `/${generateParamQuery(formatParam(queryName, filterParam), generateProjectQuery(project, false, false))}`);
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
  const queryParams = parse(search);
  cleanHistory();
  return queryParams;
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

export function resolveInitialProject(extensions: ExtensionEntry[], queryParams?: ParsedUrlQuery) {
  return parseProjectInQuery(extensions, queryParams) || newDefaultProject();
}

function normalizeQueryExtensions(queryExtensions: undefined | string | string[]): Set<string> {
  return new Set((queryExtensions ? Array.isArray(queryExtensions) ? queryExtensions : [queryExtensions] : [])
      .map(e => toShortcut(e)));
}


export function parseProjectInQuery(extensions: ExtensionEntry[], queryParams?: ParsedUrlQuery): QuarkusProject | undefined {
  if (!queryParams) {
    return undefined;
  }
  const queryExtensions = normalizeQueryExtensions(queryParams?.e);
  const selectedExtensions = _.uniqBy(extensions.filter(e => queryExtensions.has(toShortcut(e.id))), e => e.id);
  const defaultProj = newDefaultProject();
  const project = {
    metadata: {
      groupId: queryParams.g || defaultProj.metadata.groupId,
      artifactId: queryParams.a || defaultProj.metadata.artifactId,
      version: queryParams.v || defaultProj.metadata.version,
      buildTool: queryParams.b || defaultProj.metadata.buildTool,
      noExamples: queryParams.ne || defaultProj.metadata.noExamples
    },
    extensions: selectedExtensions,
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