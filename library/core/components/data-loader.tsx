import * as React from 'react';
import { useEffect, useState } from 'react';
import { effectSafety, EffectSafety, Loader } from './stuff';

export function DataLoader<T>(props: { loader: () => Promise<T>, deps?: any[], children: ((arg: T) => any) | React.ReactNode }) {
  const [ data, setData ] = useState<{ result: T } | undefined>(undefined);
  const [ error, setError ] = useState();
  const loadData = async (safety: EffectSafety) => {
    try {
      const result = await props.loader();
      safety.callSafely(() => setData({ result }));
    } catch (e) {
      safety.callSafely(() => setError(e));
    }
  };
  useEffect(() => {
    const safety = effectSafety();
    setData(undefined);
    loadData(safety);
    return safety.unload;
  // eslint-disable-next-line 
  }, ([JSON.stringify(props.deps)] || []));
  if (!!data) {
    if (props.children instanceof Function) {
      return props.children(data.result);
    }
    return props.children;
  }
  return (<Loader error={error} aria-label="Loading data"/>);
}
