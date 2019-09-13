import React, { useContext } from 'react';
import { Analytics } from './analytics';

class LoggingAnalytics implements Analytics {
    init() {
       console.log("analytics>init")
    }    
    
    pageview(path: string) {
        console.log(`analytics>pageview(${path})`)
    }

    event(category: string, action: string, label?: string, value?: number, params?: object) {
        console.log(`analytics>event(${category}, ${action}, ${label}, ${value})`)
    }
}
export const AnalyticsContext = React.createContext<Analytics>(new LoggingAnalytics());

export function useAnalytics(): Analytics {
  return useContext(AnalyticsContext);
}
