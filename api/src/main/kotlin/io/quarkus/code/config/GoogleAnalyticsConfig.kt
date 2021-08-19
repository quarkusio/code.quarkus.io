package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code.ga")
interface GoogleAnalyticsConfig {
    @get:WithName("tracking-id")
    val trackingId: Optional<String>

    @get:WithName("extensions-dimension-index")
    val extensionsDimensionIndex: OptionalInt

    @get:WithName("quarkus-version-dimension-index")
    val quarkusVersionDimensionIndex: OptionalInt

    @get:WithName("build-tool-dimension-index")
    val buildToolDimensionIndex: OptionalInt

    @get:WithName("extension-quantity-index")
    val extensionQtyDimensionIndex: OptionalInt

    @get:WithName("batching-enabled")
    val batchingEnabled: Optional<Boolean>

    @get:WithName("batchSize")
    val batchSize: OptionalInt
}