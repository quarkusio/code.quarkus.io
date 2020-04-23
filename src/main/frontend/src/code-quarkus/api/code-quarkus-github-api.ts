import { QuarkusProject } from './model';
import { BACKEND_URL, REQUEST_OPTIONS } from './env';
import { generateProjectQuery } from './quarkus-project-utils';

export async function createGitHubProject(project: QuarkusProject) {
  const data = await fetch(`${BACKEND_URL}/api/github/project?${generateProjectQuery(project)}`, {
    headers: {
      ...REQUEST_OPTIONS.headers,
      'GitHub-Code': project.github!.code,
      'GitHub-State': project.github!.state
    },
    method: 'POST'
  }).catch(() => Promise.reject(new Error('Fail to create the GitHub project')));
  if (data.ok)
    return data.json();
  if (data.status === 409) {
    throw new Error(`There is already a project named '${project.metadata.artifactId}' on your GitHub, please retry with a different name (the artifact is the name)...`);
  }
  throw new Error('Failed to create Quarkus project on GitHub');
}