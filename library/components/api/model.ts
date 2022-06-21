
export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    noCode?: boolean;
    buildTool: string;
    javaVersion: string;
  };
  extensions: string[];
  streamKey?: string;
  platformOnly?: boolean;
  github?: {
    code: string;
    state: string;
  };
}

export interface Tag {
  name: string;
  href?: string;
  description?: string;
  color?: string;
  hide?: boolean;
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
  platform: boolean;
  default: boolean;
  order: number;
  guide?: string;
  bom?: string;
}

export interface PlatformMappedExtensions {
  mapped: Extension[];
  missing: string[];
}

export interface Platform {
  extensions: Extension[];
  streams: Stream[];
  tagsDef: Tag[];
}

export interface Stream {
  key: string;
  quarkusCoreVersion: string;
  platformVersion: string;
  recommended: boolean;
  status: string;
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


