package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code.extension-processor")
interface ExtensionProcessorConfig {

    // FIXME use Optional<List<String>> when bugfix is released
    @get:WithName("tags-from")
    val tagsFrom: Optional<String>
}