package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import io.smallrye.config.WithName

@ConfigMapping(prefix = "io.quarkus.code.quarkus-platform")
interface QuarkusPlatformConfig {
    @get:WithName("group-id")
    @get:WithDefault("io.quarkus")
    val groupId: String

    @get:WithName("artifact-id")
    @get:WithDefault("quarkus-universe-bom")
    val artifactId: String

    @get:WithName("version")
    val version: String
}