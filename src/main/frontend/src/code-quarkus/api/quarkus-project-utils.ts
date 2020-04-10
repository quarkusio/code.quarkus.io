import { parse, stringify } from 'querystring';
import { ExtensionEntry } from '../pickers/extensions-picker';
import { createGitHubProject } from './code-quarkus-github-api';
import { QuarkusProject } from './model';
import { BACKEND_URL, CLIENT_NAME } from './env';

export enum Target {
  DOWNLOAD = 'DOWNLOAD',
  GITHUB = 'GITHUB'
}

export function generateProjectQuery(project: QuarkusProject, github: boolean = false): string {
  const packageName = project.metadata.packageName || project.metadata.groupId;
  const params: any = {
    ...(project.metadata.groupId && { g: project.metadata.groupId }),
    ...(project.metadata.artifactId && { a: project.metadata.artifactId }),
    ...(project.metadata.version && { v: project.metadata.version }),
    ...(project.metadata.buildTool && { b: project.metadata.buildTool }),
    ...(packageName && { c: `${packageName}.ExampleResource` }),
    ...(project.extensions && { s: project.extensions.map(e => e.shortId).join('.') }),
    cn: CLIENT_NAME
  };
  if (github) {
    params.github = true;
  }
  return stringify(params);
}

export function getProjectDownloadUrl(project: QuarkusProject) {
  return `${BACKEND_URL}/d?${generateProjectQuery(project)}`;
}


export async function generateProject(environment: string, project: QuarkusProject, target: Target): Promise<{ target: Target, url: string }> {
  switch (target) {
    case Target.DOWNLOAD:
      const url = getProjectDownloadUrl(project);
      if (environment !== 'dev') {
        setTimeout(() => window.open(url, '_blank'), 1000);
      }
      return { target, url };
    case Target.GITHUB:
      const result = await createGitHubProject(project);
      return { target, url: result.url };
  }
}

export const createOnGitHub = (project: QuarkusProject, clientId: string) => {
  const authParams = {
    redirect_uri: `${window.location.href.replace(window.location.search, '')}?${generateProjectQuery(project, true)}`,
    client_id: clientId,
    scope: 'repo',
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
      buildTool: 'MAVEN'
    },
    extensions: [],
  });
}

export function resolveInitialProject(extensions: ExtensionEntry[]) {
  return parseProjectInQuery(extensions) || newDefaultProject();
}

const defaultCleanHistory = () => {
  console.log(`remove query from url: ${window.location.search}`);
  window.history.replaceState({}, document.title, window.location.href.replace(window.location.search, ''));
};

export function parseProjectInQuery(extensions: ExtensionEntry[],
                                    search: string = window.location.search.substr(1),
                                    cleanHistory: () => void = defaultCleanHistory): QuarkusProject | undefined {
  if (search.length === 0) {
    return undefined;
  }
  const queryObj = parse(search);
  const shortIds = new Set((typeof queryObj.s === 'string') ? (queryObj.s as string).split('.') : []);
  const ids = new Set();
  const selectedExtensions = extensions.filter(e => {
    if (shortIds.has(e.shortId)) {
      const already = ids.has(e.id);
      ids.add(e.id);
      return !already;
    }
    return false;
  });
  const defaultProj = newDefaultProject();
  const project = {
    metadata: {
      groupId: queryObj.g || defaultProj.metadata.groupId,
      artifactId: queryObj.a || defaultProj.metadata.artifactId,
      version: queryObj.v || defaultProj.metadata.version,
      buildTool: queryObj.b || defaultProj.metadata.buildTool,
      packageName: (typeof queryObj.c === 'string') ? (queryObj.c as string).replace('.ExampleResource', '') : undefined,
    },
    extensions: selectedExtensions,
    github: queryObj.github === 'true' ? {
      code: queryObj.code,
      state: queryObj.state
    } : undefined
  } as QuarkusProject;
  if (project.github) {
    console.log('Received GitHub auth');
  }
  cleanHistory();
  return project;
}