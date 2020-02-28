package io.quarkus.code.analytics

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.logging.Level.FINE
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


@Singleton
open class GoogleAnalyticsService {

    companion object {
        private val LOG = Logger.getLogger(GoogleAnalyticsService::class.java.name)
        val USER_AGENT_PATTERN = Pattern.compile("^\\S+/\\S+ (\\S+).*\$")
    }

    private lateinit var defaultUserAgent: String

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

    @ConfigProperty(name = "io.quarkus.code.ga.batching-enabled", defaultValue = "true")
    internal lateinit var batchingEnabled: Provider<Boolean>

    @ConfigProperty(name = "io.quarkus.code.ga.batchSize", defaultValue = "30")
    internal lateinit var batchSize: Provider<Int>

    @ConfigProperty(name = "io.quarkus.code.hostname", defaultValue = "code.quarkus.io")
    internal lateinit var hostname: Provider<String>

    var googleAnalytics: GoogleAnalytics? = null

    fun onStart(@Observes e: StartupEvent) {
        val gaTrackingId = config.gaTrackingId.get()
        defaultUserAgent = "CodeQuarkusBackend/${config.gitCommitId.get().orElse("unknown")} (${System.getProperty("os.name")}; ${System.getProperty("os.version")}; ${System.getProperty("os.arch")}, Java ${System.getProperty("java.version")})"
        if (googleAnalytics == null && gaTrackingId.filter(String::isNotBlank).isPresent) {
            googleAnalytics = GoogleAnalytics.builder()
                    .withTrackingId(gaTrackingId.get())
                    .withConfig(GoogleAnalyticsConfig().setBatchSize(batchSize.get()).setBatchingEnabled(batchingEnabled.get()))
                    .build()
            LOG.info("GoogleAnalytics is enabled, trackingId: ${gaTrackingId.get()}")
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
            LOG.log(FINE) {
                """
                    sending analytic event:
                        - userAgent: ${fixedUserAgent}
                        - documentReferrer: ${referer}
                        - documentHostName: ${hostname.get()}
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
                    .documentHostName(hostname.get())
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