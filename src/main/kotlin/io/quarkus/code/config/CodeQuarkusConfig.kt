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

    @get:ConfigProperty(name = "environment", defaultValue = "dev")
    val environment: String

    @get:ConfigProperty(name = "sentry-dsn")
    val sentryDSN: Optional<String>

    @get:ConfigProperty(name = "hostname", defaultValue = "code.quarkus.io")
    val hostname: String
}
