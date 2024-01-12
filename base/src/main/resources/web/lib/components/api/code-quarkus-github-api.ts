import { QuarkusProject } from './model';
import { generateProjectPayload } from './quarkus-project-utils';
import { Api } from './code-quarkus-api';

export async function createGitHubProject(api: Api, project: QuarkusProject) {
  const body = JSON.stringify(generateProjectPayload(project));
  const data = await fetch(`${api.backendUrl}/github/project?cn=${api.clientName}`, {
    headers: {
      ...api.requestOptions.headers,
      'Content-Type': 'application/json',
      'GitHub-Code': project.github!.code,
      'GitHub-State': project.github!.state
    },
    method: 'POST',
    body
  }).catch(() => Promise.reject(new Error('Fail to create the GitHub project')));
  if (data.ok)
    return data.json();
  if (data.status === 409) {
    throw new Error(`There is already a project named '${project.metadata.artifactId}' on your GitHub, please retry with a different name (the artifact is the name)...`);
  }
  throw new Error('Failed to create Quarkus project on GitHub');
}