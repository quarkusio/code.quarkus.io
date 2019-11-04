package io.quarkus.code.services

import io.quarkus.code.model.Config
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.inject.Singleton

@Singleton
open class CodeQuarkusConfigManager {

    companion object {
        const val GROUPID_PATTERN = "^([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*\$"
        const val ARTIFACTID_PATTERN = "^[a-z][a-z0-9-._]*\$"
        const val CLASSNAME_PATTERN = GROUPID_PATTERN
        const val PATH_PATTERN = "^\\/([a-z0-9\\-._~%!\$&'()*+,;=:@]+\\/?)*\$"
    }


    val quarkusVersion = QuarkusExtensionCatalog.descriptor.quarkusVersion

    @ConfigProperty(name = "io.quarkus.code.git-commit-id", defaultValue = "test")
    var gitCommitId: String? = null
        private set

    @ConfigProperty(name = "io.quarkus.code.environment", defaultValue = "dev")
    lateinit var environment: String
        private set

    @ConfigProperty(name = "io.quarkus.code.ga-tracking-id", defaultValue = "")
    lateinit var gaTrackingId: String
        private set

    @ConfigProperty(name = "io.quarkus.code.sentry-dsn", defaultValue = "")
    lateinit var sentryDSN: String
        private set

    fun getConfig(): Config {
        return Config(
                environment,
                gaTrackingId,
                sentryDSN,
                quarkusVersion,
                gitCommitId
        )
    }

}