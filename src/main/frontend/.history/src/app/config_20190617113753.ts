import { checkNotNull } from '@launcher/client';

function getEnv(env: string | undefined, name: string): string | undefined {
  const globalConfig = (window as any).GLOBAL_CONFIG;
  if (globalConfig && globalConfig[name] && globalConfig[name].length > 0) {
    return globalConfig[name];
  }
  if (env && env.length === 0) {
    return undefined;
  }
  return env;
}

function requireEnv(env: string | undefined, name: string): string {
  return checkNotNull(getEnv(env, name), `process.env.${name}`);
}

export const publicUrl = process.env.PUBLIC_URL && `${process.env.PUBLIC_URL}/`;

const launcherClientApiMode = process.env.REACT_APP_CLIENT !== 'mock';

export const launcherApiUrl =
  getEnv(launcherClientApiMode ? process.env.REACT_APP_LAUNCHER_API_URL : undefined, 'launcherApiUrl');

export const sentryDsn =
  getEnv(process.env.REACT_APP_SENTRY_DSN, 'sentryDsn');
