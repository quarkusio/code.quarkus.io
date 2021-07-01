package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code.github")
interface GitHubConfig {

    @get:WithName("client-id")
    val clientId:  Optional<String>

    @get:WithName("client-secret")
    val clientSecret: Optional<String>
}