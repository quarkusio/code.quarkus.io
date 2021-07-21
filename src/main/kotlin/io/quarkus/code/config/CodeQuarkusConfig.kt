package io.quarkus.code.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.*

@ConfigMapping(prefix = "io.quarkus.code")
interface CodeQuarkusConfig {
    @get:WithName("quarkus-platform-version")
    val quarkusPlatformVersion: String

    @get:WithName("quarkus-devtools-version")
    val quarkusDevtoolsVersion: String

    @get:WithName("git-commit-id")
    val gitCommitId: String

    @get:WithName("environment")
    val environment: Optional<String>

    @get:WithName("sentry-dsn")
    val sentryDSN: Optional<String>

    // FIXME use Optional<List<String>> when bugfix is released
    @get:WithName("features")
    val features: Optional<String>

    @get:WithName("hostname")
    val hostname: Optional<String>

    @get:WithName("quarkus-platforms.reload-cron-expr")
    val quarkusPlatformReloadCronExpr: String
}
