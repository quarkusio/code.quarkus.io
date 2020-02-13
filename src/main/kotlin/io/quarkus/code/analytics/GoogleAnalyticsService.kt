package io.quarkus.code.analytics

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.runtime.StartupEvent
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class GoogleAnalyticsService {

    companion object {
        private val log = Logger.getLogger(GoogleAnalyticsService::class.java.name)
    }

    @Inject
    lateinit var config: CodeQuarkusConfigManager

    var googleAnalytics: GoogleAnalytics? = null

    fun onStart(@Observes e: StartupEvent) {
        if (googleAnalytics == null && config.gaTrackingId.get().filter(String::isNotBlank).isPresent) {
            googleAnalytics = GoogleAnalytics.builder()
                    .withTrackingId(config.gaTrackingId.get().get())
                    .withConfig(GoogleAnalyticsConfig().setBatchSize(5).setBatchingEnabled(true))
                    .build()
            log.info("GoogleAnalytics is enabled, trackingId: ${config.gaTrackingId.get().get()}")
        } else {
            log.info("GoogleAnalytics is disabled")
        }
    }

    fun sendEvent(clientName: String? = "unknown",
                  path: String,
                  url: String,
                  userAgent: String?,
                  referer: String?,
                  host: String?,
                  remoteAddr: String?
    ) {
        if (googleAnalytics != null) {
            googleAnalytics!!.event()
                    .userAgent(userAgent)
                    .documentReferrer(referer)
                    .documentHostName(host)
                    .userIp(remoteAddr)
                    .dataSource("api")
                    .anonymizeIp(true)
                    .campaignSource(clientName)
                    .eventCategory("API")
                    .documentUrl(url)
                    .documentPath(path)
                    .eventAction(path)
                    .eventLabel(clientName)
                    .sendAsync()
        } else {
            log.info("fake-analytics->event(API, $path, $clientName)")
        }

    }
}