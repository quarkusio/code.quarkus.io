
export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    noCode?: boolean;
    buildTool: string;
  };
  extensions: string[];
  streamKey?: string;
  github?: {
    code: string;
    state: string;
  };
}

export interface Tag {
  name: string;
  href?: string;
  description?: string;
  color: string;
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
  bom?: string;
}

export interface Platform {
  extensions: Extension[];
  streams: Stream[];
  tagsDef: Tag[];
}

export interface Stream {
  key: string;
  quarkusCoreVersion: string;
  recommended: boolean;
}

export interface Config {
  environment: string;
  gaTrackingId?: string;
  sentryDSN?: string;
  quarkusPlatformVersion: string;
  gitCommitId: string;
  gitHubClientId?: string;
  features: string[];
}


