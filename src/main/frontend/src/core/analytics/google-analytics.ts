import { Analytics } from './analytics';
import _ from 'lodash';

declare var ga: any;

/**
 * In order to use this implementation, the following script need to be added to the index.html
 *     <!-- Google Analytics -->
 *     <script>
 *        (function (i, s, o, g, r, a, m) {
 *         i['GoogleAnalyticsObject'] = r; i[r] = i[r] || function () {
 *             (i[r].q = i[r].q || []).push(arguments)
 *         }, i[r].l = 1 * new Date(); a = s.createElement(o),
 *             m = s.getElementsByTagName(o)[0]; a.async = 1; a.src = g; m.parentNode.insertBefore(a, m)
 *         })(window, document, 'script', 'https://www.google-analytics.com/analytics.js', 'ga');
 *     </script>
 *     <!-- End Google Analytics -->
 * 
 */
export class GoogleAnalytics implements Analytics {

    private initialized:boolean = false;

    constructor(private readonly trackingId: string){}

    init(withPageView = true) {
      if(!this.initialized) {
        ga('create', this.trackingId, 'auto');
        withPageView && ga('send', 'pageview');
        this.initialized = true;
      }
    }

    pageview(path: string) {
      ga('send', 'pageview', path);
    }

    event(category: string, action: string, label?: string, value?: number, params?: object) {
      _.defer(() => ga('send', 'event', category, action, label, value, params));
    }
}