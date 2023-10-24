import _ from 'lodash';
import { Analytics } from './analytics';


import type { AnalyticsSnippet } from "@segment/analytics-next";

declare global {
    interface Window {
        analytics: AnalyticsSnippet;
    }
}

/**
 * In order to use this implementation, the following script need to be added to the index.html
 * <script>
 *   !function(){var analytics=analytics=analytics||[];if(!analytics.initialize)if(analytics.invoked)console&&console.error&&console.error("Segment snippet included twice.");else{analytics.invoked=!0;analytics.methods=["trackSubmit","trackClick","trackLink","trackForm","pageview","identify","reset","group","track","ready","alias","debug","page","once","off","on","addSourceMiddleware","addIntegrationMiddleware","setAnonymousId","addDestinationMiddleware"];analytics.factory=function(e){return function(){var t=Array.prototype.slice.call(arguments);t.unshift(e);analytics.push(t);return analytics}};for(var e=0;e<analytics.methods.length;e++){var key=analytics.methods[e];analytics[key]=analytics.factory(key)}analytics.load=function(key,e){var t=document.createElement("script");t.type="text/javascript";t.async=!0;t.src="https://cdn.segment.com/analytics.js/v1/" + key + "/analytics.min.js";var n=document.getElementsByTagName("script")[0];n.parentNode.insertBefore(t,n);analytics._loadOptions=e};analytics._writeKey="YOUR_WRITE_KEY";analytics.SNIPPET_VERSION="4.15.2";
 *   analytics.load("YOUR_WRITE_KEY");
 *   analytics.page();
 *   }}();
 * </script>
 *
 */
export class SegmentAnalyticsImpl implements Analytics {

    private initialized:boolean = false;

    constructor(private readonly writeKey: string){}

    init(withPageView = true) {
      if(!window.analytics) {
          throw new Error("Unable to init Segment Analytics");
      }
      if(!this.initialized) {
          window.analytics.load(this.writeKey);
        withPageView && this.pageview(null);
        this.initialized = true;
      }
    }

    pageview(path: string) {
        window.analytics.page(path);
    }

    event(event: string, params?: object): void {
      _.defer(() => {
          window.analytics.track(event, {
              event,
              ...params
          })
      });
    }
}