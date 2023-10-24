package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code.quarkus-platforms")
interface PlatformConfig {

    @get:WithName("reload-cron-expr")
    val reloadCronExpr: String

    @get:WithName("registry-id")
    val registryId: Optional<String>
}
