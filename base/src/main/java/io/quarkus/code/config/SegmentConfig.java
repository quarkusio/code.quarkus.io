package io.quarkus.code.config;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;
import java.util.OptionalInt;

@ConfigMapping(prefix = "io.quarkus.code.segment")
public interface SegmentConfig {
    Optional<String> writeKey();

    OptionalInt flushQueueSize();

    OptionalInt flushIntervalSeconds();

    default String writeKeyForDisplay() {
        return writeKey().filter(s -> !s.isBlank()).orElse("UNDEFINED");
    }
}