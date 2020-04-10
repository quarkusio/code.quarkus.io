package io.quarkus.code.config

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ConfigProperties(prefix = "io.quarkus.code.github")
interface GitHubConfig {

    @get:ConfigProperty(name = "client-id")
    val clientId:  Optional<String>

    @get:ConfigProperty(name = "client-secret")
    val clientSecret: Optional<String>
}