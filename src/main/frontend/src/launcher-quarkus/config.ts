
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

export const publicUrl = process.env.PUBLIC_URL && `${process.env.PUBLIC_URL}/`;
