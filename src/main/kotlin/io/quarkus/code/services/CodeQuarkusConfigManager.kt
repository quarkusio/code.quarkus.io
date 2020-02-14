package io.quarkus.code.services

import io.quarkus.code.model.Config
import io.quarkus.code.services.QuarkusExtensionCatalog.Companion.bundledQuarkusVersion
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
open class CodeQuarkusConfigManager {
    internal val quarkusVersion = bundledQuarkusVersion

    @ConfigProperty(name = "io.quarkus.code.git-commit-id")
    internal lateinit var gitCommitId: Optional<String>

    @ConfigProperty(name = "io.quarkus.code.environment", defaultValue = "dev")
    internal lateinit var environment: String

    @ConfigProperty(name = "io.quarkus.code.ga.tracking-id")
    internal lateinit var gaTrackingId: Optional<String>

    @ConfigProperty(name = "io.quarkus.code.sentry-dsn")
    internal lateinit var sentryDSN: Optional<String>

    fun getConfig(): Config {
        return Config(
                environment,
                gaTrackingId.orElse(null),
                sentryDSN.orElse(null),
                quarkusVersion,
                gitCommitId.orElse(null)
        )
    }

}
