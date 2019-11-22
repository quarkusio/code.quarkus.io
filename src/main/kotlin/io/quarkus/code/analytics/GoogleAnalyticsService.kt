package io.quarkus.code.analytics

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig
import com.brsanthu.googleanalytics.request.DefaultRequest
import javax.inject.Singleton
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.logging.Logger
import javax.enterprise.event.Observes



@Singleton
open class GoogleAnalyticsService {

    companion object {
        private val log = Logger.getLogger(GoogleAnalyticsService::class.java.name)
    }

    @ConfigProperty(name = "io.quarkus.code.ga-tracking-id", defaultValue = "")
    lateinit var googleTrackingId: String

    var googleAnalytics: GoogleAnalytics? = null

    fun onStart(@Observes e: StartupEvent) {
        if (googleAnalytics == null && googleTrackingId.isNotEmpty()) {
            val defaultRequest = DefaultRequest()

            defaultRequest.documentHostName("code.quarkus.io")
            defaultRequest.documentReferrer("https://code.quarkus.io")

            googleAnalytics = GoogleAnalytics.builder()
                    .withDefaultRequest(defaultRequest)
                    .withTrackingId(googleTrackingId)
                    .withConfig(GoogleAnalyticsConfig().setBatchSize(30).setBatchingEnabled(true))
                    .build()
            log.info("GoogleAnalytics is enabled, trackingId: $googleTrackingId")
        } else {
            log.info("GoogleAnalytics is disabled")
        }
    }

    fun sendEvent(clientName: String? = "unknown", action: String, label: String) {
        if(googleAnalytics != null) {
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