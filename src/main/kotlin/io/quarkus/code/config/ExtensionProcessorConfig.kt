package io.quarkus.code.config

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ConfigProperties(prefix = "io.quarkus.code.extension-processor")
interface ExtensionProcessorConfig {

    @get:ConfigProperty(name = "tags-from", defaultValue = "status")
    val tagsFrom: List<String>
}