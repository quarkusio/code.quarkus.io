import { ExtensionEntry } from '../pickers/extensions-picker';

export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    noExamples?: boolean;
    buildTool: string;
  };
  extensions: ExtensionEntry[];
  github?: {
    code: string;
    state: string;
  };
}

export interface Extension {
  id: string;
  shortId: string;
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
