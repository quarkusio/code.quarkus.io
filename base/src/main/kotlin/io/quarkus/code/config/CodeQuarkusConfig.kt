package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code")
interface CodeQuarkusConfig {

    @get:WithName("id")
    val id: String

    @get:WithName("name")
    val name: String

    @get:WithName("quarkus-platform-version")
    val quarkusPlatformVersion: Optional<String>

    @get:WithName("quarkus-devtools-version")
    val quarkusDevtoolsVersion: Optional<String>

    @get:WithName("git-commit-id")
    val gitCommitId: Optional<String>

    @get:WithName("environment")
    val environment: Optional<String>

    @get:WithName("sentry-frontend-dsn")
    val sentryFrontendDSN: Optional<String>

    // FIXME use Optional<List<String>> when bugfix is released
    @get:WithName("features")
    val features: Optional<String>

    @get:WithName("hostname")
    val hostname: Optional<String>
}
