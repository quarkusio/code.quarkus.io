package io.quarkus.code

import io.quarkus.code.quarkus.QuarkusExtensionCatalog.Companion.bundledQuarkusVersion
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.inject.Singleton

@Singleton
open class CodeQuarkusConfig {

    companion object {
        const val GROUPID_PATTERN = "^([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*\$"
        const val ARTIFACTID_PATTERN = "^[a-z][a-z0-9-._]*\$"
        const val CLASSNAME_PATTERN = GROUPID_PATTERN
        const val PATH_PATTERN = "^\\/([a-z0-9\\-._~%!\$&'()*+,;=:@]+\\/?)*\$"
    }

    val quarkusVersion = bundledQuarkusVersion

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

    @ConfigProperty(name = "io.quarkus.code.github.clientId", defaultValue = "")
    lateinit var clientId: String
        private set

    @ConfigProperty(name = "io.quarkus.code.github.clientSecret", defaultValue = "")
    lateinit var clientSecret: String
        private set

}