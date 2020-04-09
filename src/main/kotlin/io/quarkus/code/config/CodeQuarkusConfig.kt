package io.quarkus.code.config

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ConfigProperties(prefix = "io.quarkus.code")
interface CodeQuarkusConfig {
    @get:ConfigProperty(name = "quarkus-version")
    val quarkusVersion: String

    @get:ConfigProperty(name = "git-commit-id")
    val gitCommitId: String

    @get:ConfigProperty(name = "environment")
    val environment: Optional<String>

    @get:ConfigProperty(name = "sentry-dsn")
    val sentryDSN: Optional<String>

    // FIXME use Optional<List<String>> when bugfix is released
    @get:ConfigProperty(name = "features")
    val features: Optional<String>

    @get:ConfigProperty(name = "hostname")
    val hostname: Optional<String>
}
