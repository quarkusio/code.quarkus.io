package io.quarkus.code.analytics

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.ConfigProvider
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


@Singleton
open class GoogleAnalyticsService {

    companion object {
        private val log = Logger.getLogger(GoogleAnalyticsService::class.java.name)
        private val defaultUserAgent = "Java/${System.getProperty("java.version")} (${System.getProperty("os.name")} ${System.getProperty("os.version")}; ${System.getProperty("os.arch")})"
    }

    @Inject
    lateinit var config: CodeQuarkusConfigManager

    @ConfigProperty(name = "io.quarkus.code.ga.extensions-dimension-index", defaultValue = "-1")
    internal lateinit var extensionsDimensionIndex: Provider<Int>

    @ConfigProperty(name = "io.quarkus.code.ga.quarkus-version-dimension-index", defaultValue = "-1")
    internal lateinit var quarkusVersionDimensionIndex: Provider<Int>

    @ConfigProperty(name = "io.quarkus.code.ga.build-tool-dimension-index", defaultValue = "-1")
    internal lateinit var buildToolDimensionIndex: Provider<Int>

    @ConfigProperty(name = "io.quarkus.code.ga.extension-quantity-index", defaultValue = "-1")
    internal lateinit var extensionQtyDimensionIndex: Provider<Int>

    var googleAnalytics: GoogleAnalytics? = null

    fun onStart(@Observes e: StartupEvent) {
        val gaTrackingId = config.gaTrackingId.get()
        if (googleAnalytics == null && gaTrackingId.filter(String::isNotBlank).isPresent) {
            googleAnalytics = GoogleAnalytics.builder()
                    .withTrackingId(gaTrackingId.get())
                    .withConfig(GoogleAnalyticsConfig().setBatchSize(20).setBatchingEnabled(true))
                    .build()
            log.info("GoogleAnalytics is enabled, trackingId: ${gaTrackingId.get()}")
        } else {
            log.info("GoogleAnalytics is disabled")
        }
    }

    fun sendEvent(
            category: String,
            action: String,
            label: String,
            clientName: String,
            path: String,
            url: String,
            userAgent: String?,
            referer: String?,
            host: String?,
            remoteAddr: String?,
            extensions: Set<String>?,
            buildTool: String?
    ) {
        if (googleAnalytics != null) {
            val event = googleAnalytics!!.event()
            if (extensions != null && extensionsDimensionIndex.get() >= 0) {
                event.customDimension(extensionsDimensionIndex.get(), extensions.sorted().joinToString(","))
            }
            if (extensions != null && extensionQtyDimensionIndex.get() >= 0) {
                event.customDimension(extensionQtyDimensionIndex.get(), extensions.size.toString())
            }
            if (quarkusVersionDimensionIndex.get() >= 0) {
                event.customDimension(quarkusVersionDimensionIndex.get(), config.quarkusVersion)
            }
            if (buildTool != null && buildToolDimensionIndex.get() >= 0) {
                event.customDimension(buildToolDimensionIndex.get(), buildTool)
            }
            if (category == "API" && action == "/download") {
                log.log(Level.INFO) {
                    """
                    sending analytic event:
                        - userAgent: ${userAgent ?: defaultUserAgent}
                        - documentReferrer: ${referer}
                        - documentHostName: ${host}
                        - userIp: ${remoteAddr != null}
                        - campaignSource: ${clientName}
                        - documentUrl: ${url}
                        - documentPath: ${path}
                        - eventCategory: ${category}
                        - eventAction: ${action}
                        - eventLabel: ${label}
                    """.trimIndent()
                }
            }
            event
                    .userAgent(userAgent ?: defaultUserAgent)
                    .documentReferrer(referer)
                    .documentHostName(host)
                    .userIp(remoteAddr)
                    .dataSource("api")
                    .anonymizeIp(true)
                    .campaignSource(clientName)
                    .eventCategory(category)
                    .documentUrl(url)
                    .documentPath(path)
                    .eventAction(action)
                    .eventLabel(label)
                    .sendAsync()
        } else {
            log.info("fake-analytics->event(API, $path, $clientName)")
        }

    }
}