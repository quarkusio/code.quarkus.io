
export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    noCode?: boolean;
    buildTool: string;
  };
  extensions: Extension[];
  github?: {
    code: string;
    state: string;
  };
}

export interface Extension {
  id: string;
  version: string;
  name: string;
  keywords: string[];
  tags: string[];
  description?: string;
  shortName?: string;
  category: string;
  default: boolean;
  order: number;
  guide?: string;
}

export interface Config {
  environment: string;
  gaTrackingId?: string;
  sentryDSN?: string;
  quarkusVersion: string;
  gitCommitId: string;
  gitHubClientId?: string;
  features: string[];
}
