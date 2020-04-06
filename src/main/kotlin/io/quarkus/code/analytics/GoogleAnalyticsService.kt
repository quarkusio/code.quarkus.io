package io.quarkus.code.analytics

import com.brsanthu.googleanalytics.GoogleAnalytics
import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.GoogleAnalyticsConfig
import io.quarkus.runtime.StartupEvent
import java.util.logging.Level.FINE
import java.util.logging.Level.INFO
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GoogleAnalyticsService {

    companion object {
        private val LOG = Logger.getLogger(GoogleAnalyticsService::class.java.name)
        val USER_AGENT_PATTERN = Pattern.compile("^\\S+/\\S+ (\\S+).*\$")
    }

    private lateinit var defaultUserAgent: String

    @Inject
    lateinit var config: CodeQuarkusConfig

    @Inject
    lateinit var gaConfig: GoogleAnalyticsConfig

    var googleAnalytics: GoogleAnalytics? = null

    fun onStart(@Observes e: StartupEvent) {
        val gaTrackingId = gaConfig.trackingId
        defaultUserAgent = "CodeQuarkusBackend/${config.gitCommitId} (${System.getProperty("os.name")}; ${System.getProperty("os.version")}; ${System.getProperty("os.arch")}, Java ${System.getProperty("java.version")})"
        if (googleAnalytics == null && gaTrackingId.filter(String::isNotBlank).isPresent) {
            val batching = gaConfig.batchingEnabled
            val batchSize = gaConfig.batchSize
            googleAnalytics = GoogleAnalytics.builder()
                    .withTrackingId(gaTrackingId.get())
                    .withConfig(com.brsanthu.googleanalytics.GoogleAnalyticsConfig().setBatchSize(batchSize).setBatchingEnabled(batching))
                    .build()
            LOG.log(INFO) {"""
                GoogleAnalytics is enabled:
                    trackingId: ${gaTrackingId.get()}
                    batchSize: $batchSize
                    batchingEnabled: $batching
                    hostname: ${config.hostname}
                    defaultUserAgent: $defaultUserAgent
                    extensionsDimensionIndex: ${gaConfig.extensionsDimensionIndex.orElse(-1)}
                    extensionQtyDimensionIndex: ${gaConfig.extensionQtyDimensionIndex.orElse(-1)}
                    quarkusVersionDimensionIndex: ${gaConfig.quarkusVersionDimensionIndex.orElse(-1)}
                    buildToolDimensionIndex: ${gaConfig.buildToolDimensionIndex.orElse(-1)}
                    extensionsDimensionIndex: ${gaConfig.extensionsDimensionIndex.orElse(-1)}
            """.trimIndent()}
        } else {
            LOG.info("GoogleAnalytics is disabled")
        }
    }

    fun sendEvent(
            category: String,
            action: String,
            label: String,
            applicationName: String,
            path: String,
            url: String,
            userAgent: String?,
            referer: String?,
            remoteAddr: String?,
            extensions: Set<String>?,
            buildTool: String?
    ) {
        val fixedUserAgent = fixUserAgent(userAgent)
        if (googleAnalytics != null) {
            val event = googleAnalytics!!.event()
            if (extensions != null && gaConfig.extensionsDimensionIndex.isPresent) {
                event.customDimension(gaConfig.extensionsDimensionIndex.asInt, extensions.sorted().joinToString(","))
            }
            if (extensions != null && gaConfig.extensionQtyDimensionIndex.isPresent) {
                event.customDimension(gaConfig.extensionQtyDimensionIndex.asInt, extensions.size.toString())
            }
            if (gaConfig.quarkusVersionDimensionIndex.isPresent) {
                event.customDimension(gaConfig.quarkusVersionDimensionIndex.asInt, config.quarkusVersion)
            }
            if (buildTool != null && gaConfig.buildToolDimensionIndex.isPresent) {
                event.customDimension(gaConfig.buildToolDimensionIndex.asInt, buildTool)
            }
            LOG.log(FINE) {
                """
                    sending analytic event:
                        - userAgent: ${fixedUserAgent}
                        - documentReferrer: ${referer}
                        - documentHostName: ${config.hostname}
                        - userIp: ${remoteAddr != null}
                        - applicationName: ${applicationName}
                        - documentUrl: ${url}
                        - documentPath: ${path}
                        - eventCategory: ${category}
                        - eventAction: ${action}
                        - eventLabel: ${label}
                    """.trimIndent()
            }
            event
                    .userAgent(fixedUserAgent)
                    .documentReferrer(referer)
                    .documentHostName(config.hostname)
                    .userIp(remoteAddr)
                    .dataSource("api")
                    .anonymizeIp(true)
                    .applicationName(applicationName)
                    .eventCategory(category)
                    .documentUrl(url)
                    .documentPath(path)
                    .eventAction(action)
                    .eventLabel(label)
                    .sendAsync()
        } else {
            LOG.info("fake-analytics->event($category, $action, $label)")
        }

    }

    private fun fixUserAgent(userAgent: String?): String {
        if (userAgent.isNullOrBlank() || !USER_AGENT_PATTERN.matcher(userAgent).matches()) {
            return defaultUserAgent
        }
        return userAgent
    }
}