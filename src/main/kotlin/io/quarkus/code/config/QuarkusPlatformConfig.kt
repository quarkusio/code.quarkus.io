package io.quarkus.code.config

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty

@ConfigProperties(prefix = "io.quarkus.code.quarkus-platform")
interface QuarkusPlatformConfig {
    @get:ConfigProperty(name = "group-id", defaultValue = "io.quarkus")
    val groupId: String

    @get:ConfigProperty(name = "artifact-id", defaultValue = "quarkus-universe-bom")
    val artifactId: String

    @get:ConfigProperty(name = "version")
    val version: String
}