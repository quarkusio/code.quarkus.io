package io.quarkus.code.service


import com.segment.analytics.Analytics
import com.segment.analytics.messages.TrackMessage
import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.SegmentConfig
import java.util.concurrent.TimeUnit
import java.util.logging.Level.FINE
import java.util.logging.Level.INFO
import java.util.logging.Logger
import java.util.regex.Pattern
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class SegmentAnalyticsService @Inject constructor(val config: CodeQuarkusConfig, val segmentConfig: SegmentConfig) {

    companion object {
        private val LOG = Logger.getLogger(SegmentAnalyticsService::class.java.name)
        val USER_AGENT_PATTERN = Pattern.compile("^\\S+/\\S+ (\\S+).*\$")
    }

    private lateinit var defaultUserAgent: String

    val analytics: Analytics?

    init {
        this.analytics = createAnalytics()
    }


    fun createAnalytics(): Analytics? {
        val writeKey = segmentConfig.writeKey
        defaultUserAgent =
            "CodeQuarkusBackend/${config.gitCommitId} (${System.getProperty("os.name")}; ${System.getProperty("os.version")}; ${
                System.getProperty("os.arch")
            }, Java ${System.getProperty("java.version")})"
        if (analytics == null && writeKey.filter(String::isNotBlank).isPresent) {
            val flushQueueSize = segmentConfig.flushQueueSize.orElse(30)
            val flushIntervalSeconds = segmentConfig.flushIntervalSeconds.orElse(120)
            LOG.log(INFO) {
                """
                Segment Analytics is enabled:
                    writeKey: ${segmentConfig.writeKeyForDisplay()}
                    flushQueueSize: $flushQueueSize
                    flushIntervalSeconds: $flushIntervalSeconds
                    hostname: ${config.hostname}
                    defaultUserAgent: $defaultUserAgent
            """.trimIndent()
            }
            return Analytics.builder(writeKey.get())
                .flushInterval(flushIntervalSeconds.toLong(), TimeUnit.SECONDS)
                .flushQueueSize(flushQueueSize)
                .build()
        }
        LOG.info("Segment Analytics is disabled")
        return null
    }

    fun track(
        event: String,
        properties: Map<String, Any?> = mapOf(),
        source: String,
        path: String,
        url: String,
        userAgent: String?,
        referer: String?,
        anonymousId: String?
    ) {
        val fixedUserAgent = fixUserAgent(userAgent)
        var prefix = "[Segment] "
        var level = FINE
        val hostName = config.hostname.orElse("code.quarkus.io")
        if (analytics != null) {
            val props = mapOf(
                "hostName" to hostName,
                "source" to source,
                "path" to path,
                "url" to url,
                "requestHeaders" to mapOf("userAgent" to userAgent, "referer" to referer)
            )
            analytics!!.enqueue(TrackMessage.builder(event)
                .anonymousId(anonymousId)
                .properties(properties)
                .context(props))
        } else {
            prefix = "[Disabled] "
            level = INFO
        }
        LOG.log(level) {
            """
                $prefix sending analytics event "$event":
                    - properties: $properties
                    - userAgent: $fixedUserAgent
                    - referer: $referer
                    - hostName: $hostName
                    - anonymousId: $anonymousId
                    - source: $source
                    - documentUrl: $url
                    - documentPath: $path
                """.trimIndent()
        }

    }

    private fun fixUserAgent(userAgent: String?): String {
        if (userAgent.isNullOrBlank() || !USER_AGENT_PATTERN.matcher(userAgent).matches()) {
            return defaultUserAgent
        }
        return userAgent
    }
}