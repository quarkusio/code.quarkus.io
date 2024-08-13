
export interface QuarkusProject {
  metadata: {
    groupId: string;
    artifactId: string;
    version: string;
    name?: string;
    noCode?: boolean;
    buildTool: string;
    javaVersion?: string;
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

export interface Preset {
  key: string;
  title: string;
  icon: string;
  extensions: string[];
  resolvedExtensions?: Extension[];
}

export interface PlatformMappedExtensions {
  mapped: Extension[];
  missing: string[];
}

export interface Platform {
  extensions: Extension[];
  streams: Stream[];
  presets: Preset[];
  tagsDef: Tag[];
}

export interface JavaCompatibility {
  versions: number[];
  recommended: number;
}

export interface Stream {
  key: string;
  quarkusCoreVersion: string;
  platformVersion: string;
  recommended: boolean;
  status: string;
  lts: boolean;
  javaCompatibility: JavaCompatibility;
}

export interface Config {
  environment: string;
  segmentWriteKey?: string;
  sentryDSN?: string;
  quarkusPlatformVersion: string;
  gitCommitId: string;
  gitHubClientId?: string;
  features: string[];
}


