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
import javax.inject.Singleton


@Singleton
open class GoogleAnalyticsService {

    companion object {
        private val log = Logger.getLogger(GoogleAnalyticsService::class.java.name)
        private val defaultUserAgent = "Java/${System.getProperty("java.version")} (${System.getProperty("os.name")} ${System.getProperty("os.version")}; ${System.getProperty("os.arch")})"
    }

    @Inject
    lateinit var config: CodeQuarkusConfigManager

    @ConfigProperty(name = "io.quarkus.code.ga.extensions-dimension-index")
    internal lateinit var extensionsDimensionIndex: Optional<Int>

    @ConfigProperty(name = "io.quarkus.code.ga.quarkus-version-dimension-index")
    internal lateinit var quarkusVersionDimensionIndex: Optional<Int>

    @ConfigProperty(name = "io.quarkus.code.ga.build-tool-dimension-index")
    internal lateinit var buildToolDimensionIndex: Optional<Int>

    @ConfigProperty(name = "io.quarkus.code.ga.extension-quantity-index")
    internal lateinit var extensionQtyDimensionIndex: Optional<Int>

    var googleAnalytics: GoogleAnalytics? = null

    fun onStart(@Observes e: StartupEvent) {
        val gaTrackingId = ConfigProvider.getConfig().getOptionalValue("io.quarkus.code.ga.tracking-id", String::class.java)
        if (googleAnalytics == null && gaTrackingId.isPresent) {
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
            if (extensions != null && extensionsDimensionIndex.isPresent) {
                event.customDimension(extensionsDimensionIndex.get(), extensions.sorted().joinToString(","))
            }
            if (extensions != null && extensionQtyDimensionIndex.isPresent) {
                event.customDimension(extensionQtyDimensionIndex.get(), extensions.size.toString())
            }
            if (quarkusVersionDimensionIndex.isPresent) {
                event.customDimension(quarkusVersionDimensionIndex.get(), config.quarkusVersion)
            }
            if (buildTool != null && buildToolDimensionIndex.isPresent) {
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