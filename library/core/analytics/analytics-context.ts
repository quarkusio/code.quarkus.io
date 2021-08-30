import React, {useContext, useState} from 'react';
import { Analytics } from './analytics';

class LoggingAnalytics implements Analytics {
  init() {
    console.log('analytics>init');
  }

  pageview(path: string) {
    console.log(`analytics>pageview(${path})`);
  }

  event(category: string, action: string, label?: string, value?: number, params?: object) {
    console.log(`analytics>event(${category}, ${action}, ${label}, ${value})`);
  }
}

export const AnalyticsContext = React.createContext<Analytics>(new LoggingAnalytics());

export function createLinkTracker(analytic: Analytics, category = 'UX', label: string, attr = 'href') {
  return (e: any) => {
    const attrVal = e.target?.getAttribute?.(attr);
    if (!!attrVal) {
      analytic.event(category, label, attrVal);
    }
  };
}

export function useAnalyticsEditionField(id: string, onChange: any): [boolean, (event: any) => void] {
  const [ isDirty, setIsDirty ] = useState(false);
  const analytics = useAnalytics();
  const onChangeWithDirty = (event: any) => {
    if (!isDirty) {
      analytics.event('UX', 'Edit field', id);
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
