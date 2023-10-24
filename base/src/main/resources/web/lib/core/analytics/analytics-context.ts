import React, {useContext, useState} from 'react';
import { Analytics } from './analytics';

class LoggingAnalytics implements Analytics {
  init() {
    console.log('analytics>init');
  }

  pageview(path: string) {
    console.log(`analytics>pageview(${path})`);
  }

  event(event: string, context: object): void {
    console.log(`analytics>event(${event}, ${JSON.stringify(context)})`);
  }
}

export const AnalyticsContext = React.createContext<Analytics>(new LoggingAnalytics());

export function createLinkTracker(analytic: Analytics, attr = 'href', context: object = {}) {
  return (e: any) => {
    const attrVal = e.target?.getAttribute?.(attr);
    if (!!attrVal) {
      analytic.event('Click', { label: attrVal, ...context });
    }
  };
}

export function useAnalyticsEditionField(id: string, onChange: any, context: object = {}): [boolean, (event: any) => void] {
  const [ isDirty, setIsDirty ] = useState(false);
  const analytics = useAnalytics();
  const onChangeWithDirty = (event: any) => {
    if (!isDirty) {
      analytics.event('Edit field', { field: id, ...context });
    }
    setIsDirty(true);
    if (onChange) {
      onChange(event);
    }
  };
  return [ isDirty, onChangeWithDirty ];
}

export function useAnalytics(): Analytics {
  return useContext(AnalyticsContext);
}
