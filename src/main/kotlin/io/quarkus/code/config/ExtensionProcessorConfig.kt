package io.quarkus.code.config

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ConfigProperties(prefix = "io.quarkus.code.extension-processor")
interface ExtensionProcessorConfig {

    // FIXME use Optional<List<String>> when bugfix is released
    @get:ConfigProperty(name = "tags-from")
    val tagsFrom: Optional<String>
}