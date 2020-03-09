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

    @ConfigProperty(name = "io.quarkus.code.git-commit-id", defaultValue = "")
    internal lateinit var gitCommitId: Provider<Optional<String>>

    @ConfigProperty(name = "io.quarkus.code.environment", defaultValue = "dev")
    internal lateinit var environment: Provider<String>

    @ConfigProperty(name = "io.quarkus.code.ga.tracking-id", defaultValue = "")
    internal lateinit var gaTrackingId: Provider<Optional<String>>

    @ConfigProperty(name = "io.quarkus.code.sentry-dsn", defaultValue = "")
    internal lateinit var sentryDSN: Provider<Optional<String>>

    fun getConfig(): Config {
        return Config(
                environment.get(),
                gaTrackingId.get().filter(String::isNotBlank).orElse(null),
                sentryDSN.get().filter(String::isNotBlank).orElse(null),
                quarkusVersion,
                gitCommitId.get().filter(String::isNotBlank).orElse(null)
        )
    }

}
