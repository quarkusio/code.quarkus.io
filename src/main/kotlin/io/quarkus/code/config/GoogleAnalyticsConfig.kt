package io.quarkus.code.config

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ConfigProperties(prefix = "io.quarkus.code.ga")
interface GoogleAnalyticsConfig {
    @get:ConfigProperty(name = "tracking-id")
    val trackingId: Optional<String>

    @get:ConfigProperty(name = "extensions-dimension-index")
    val extensionsDimensionIndex: OptionalInt

    @get:ConfigProperty(name = "quarkus-version-dimension-index")
    val quarkusVersionDimensionIndex: OptionalInt

    @get:ConfigProperty(name = "build-tool-dimension-index")
    val buildToolDimensionIndex: OptionalInt

    @get:ConfigProperty(name = "extension-quantity-index")
    val extensionQtyDimensionIndex: OptionalInt

    @get:ConfigProperty(name = "batching-enabled")
    val batchingEnabled: Optional<Boolean>

    @get:ConfigProperty(name = "batchSize")
    val batchSize: OptionalInt
}