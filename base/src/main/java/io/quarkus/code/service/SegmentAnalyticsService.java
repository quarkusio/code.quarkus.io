package io.quarkus.code.service;

import com.google.common.base.Strings;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.TrackMessage;
import io.quarkus.code.config.CodeQuarkusConfig;
import io.quarkus.code.config.SegmentConfig;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

@Singleton
public class SegmentAnalyticsService {

    public static final Pattern USER_AGENT_PATTERN = Pattern.compile("^\\S+/\\S+ (\\S+).*$");

    private String defaultUserAgent;
    private final CodeQuarkusConfig config;
    private final SegmentConfig segmentConfig;
    private final Analytics analytics;

    @Inject
    public SegmentAnalyticsService(CodeQuarkusConfig config, SegmentConfig segmentConfig) {
        this.config = config;
        this.segmentConfig = segmentConfig;
        this.analytics = createAnalytics();
    }

    private Analytics createAnalytics() {
        Optional<String> writeKey = segmentConfig.writeKey();
        defaultUserAgent = "CodeQuarkusBackend/" + config.gitCommitId() + " (" +
                System.getProperty("os.name") + "; " +
                System.getProperty("os.version") + "; " +
                System.getProperty("os.arch") + ", Java " +
                System.getProperty("java.version") + ")";
        if (analytics == null && writeKey.filter(not(Strings::isNullOrEmpty)).isPresent()) {
            int flushQueueSize = segmentConfig.flushQueueSize().orElse(30);
            int flushIntervalSeconds = segmentConfig.flushIntervalSeconds().orElse(120);
            Log.infof("""
                    Segment Analytics is enabled:
                        writeKey: %s
                        flushQueueSize: %s
                        flushIntervalSeconds: %s
                        hostname: %s
                        defaultUserAgent: %s
                    """.stripIndent(),
                    segmentConfig.writeKeyForDisplay(), flushQueueSize, flushIntervalSeconds, config.hostname(), defaultUserAgent);
            return Analytics.builder(writeKey.get())
                    .flushInterval(flushIntervalSeconds, TimeUnit.SECONDS)
                    .flushQueueSize(flushQueueSize)
                    .build();
        }
        Log.info("Segment Analytics is disabled");
        return null;
    }

    public void track(String event, Map<String, Object> properties, String source, String path, String url, String userAgent, String referer, String anonymousId) {
        String fixedUserAgent = fixUserAgent(userAgent);
        String prefix = "[Segment] ";
        Level level = Level.DEBUG;
        String hostName = config.hostname().orElse("code.quarkus.io");
        if (analytics != null) {
            Map<String, Object> props = Map.of(
                    "hostName", hostName,
                    "source", source,
                    "path", path,
                    "url", url,
                    "requestHeaders", Map.of("userAgent", userAgent, "referer", referer)
            );
            analytics.enqueue(TrackMessage.builder(event)
                    .anonymousId(anonymousId)
                    .properties(properties)
                    .context(props));
        } else {
            prefix = "[Disabled] ";
            level = Level.INFO;
        }
        Log.logf(level, """
                %s sending analytics event "%s":
                    - properties: %s
                    - userAgent: %s
                    - referer: %s
                    - hostName: %s
                    - anonymousId: %s
                    - source: %s
                    - documentUrl: %s
                    - documentPath: %s
                """.stripIndent(),
                prefix, event, properties, fixedUserAgent, referer, hostName, anonymousId, source, url, path);
    }

    private String fixUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank() || !USER_AGENT_PATTERN.matcher(userAgent).matches()) {
            return defaultUserAgent;
        }
        return userAgent;
    }
}