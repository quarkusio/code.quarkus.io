package io.quarkus.code

import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness
import javax.inject.Inject
import javax.inject.Singleton


@Readiness
@Singleton
open class CodeQuarkusHealthCheck : HealthCheck {
    @Inject
    internal var configManager: CodeQuarkusConfigManager? = null

    @Inject
    internal var extensionCatalog: QuarkusExtensionCatalog? = null

    @Inject
    internal var projectCreator: QuarkusProjectCreator? = null

    override fun call(): HealthCheckResponse {
        val responseBuilder = HealthCheckResponse.named("Code Quarkus HealthCheck")
        if(configManager?.getConfig() != null
                && extensionCatalog?.extensions != null
                && projectCreator != null) {
            responseBuilder.up()
        } else {
            responseBuilder.down()
        }
        return responseBuilder.build()
    }

}