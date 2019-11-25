package io.quarkus.code.analytics

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig
import com.brsanthu.googleanalytics.request.DefaultRequest
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
        if (googleAnalytics == null && config.gaTrackingId.get().isNotEmpty()) {
            val defaultRequest = DefaultRequest()

            defaultRequest.documentHostName("code.quarkus.io")

            googleAnalytics = GoogleAnalytics.builder()
                    .withDefaultRequest(defaultRequest)
                    .withTrackingId(config.gaTrackingId.get())
                    .withConfig(GoogleAnalyticsConfig().setBatchSize(5).setBatchingEnabled(true))
                    .build()
            log.info("GoogleAnalytics is enabled, trackingId: ${config.gaTrackingId.get()}")
        } else {
            log.info("GoogleAnalytics is disabled")
        }
    }

    fun sendEvent(clientName: String? = "unknown", action: String, label: String) {
        if (googleAnalytics != null) {
            googleAnalytics!!.event()
                    .campaignSource(clientName)
                    .eventCategory("API")
                    .eventAction(action)
                    .eventLabel(label)
                    .sendAsync()
        } else {
            log.info("fake-analytics->event(API, $action, $label) from $clientName")
        }

    }
}