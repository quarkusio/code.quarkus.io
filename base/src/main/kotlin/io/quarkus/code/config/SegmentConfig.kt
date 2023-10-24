package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code.segment")
interface SegmentConfig {
    @get:WithName("write-key")
    val writeKey: Optional<String>

    @get:WithName("flushQueueSize")
    val flushQueueSize: OptionalInt

    @get:WithName("flushIntervalSeconds")
    val flushIntervalSeconds: OptionalInt

    fun writeKeyForDisplay(): String {
        return writeKey.filter(String::isNotBlank).map { it }.orElse("UNDEFINED")
    }
}