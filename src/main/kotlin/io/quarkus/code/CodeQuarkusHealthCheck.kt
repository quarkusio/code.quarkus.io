package io.quarkus.code

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness
import javax.inject.Inject
import javax.inject.Singleton


@Readiness
@Singleton
class CodeQuarkusHealthCheck : HealthCheck {
    @Inject
    internal var config: CodeQuarkusConfig? = null

    @Inject
    internal var extensionCatalog: QuarkusExtensionCatalog? = null

    @Inject
    internal var projectCreator: QuarkusProjectCreator? = null

    override fun call(): HealthCheckResponse {
        val responseBuilder = HealthCheckResponse.named("Code Quarkus HealthCheck")
        if(config != null
                && extensionCatalog?.extensions != null
                && projectCreator != null) {
            responseBuilder.up()
        } else {
            responseBuilder.down()
        }
        return responseBuilder.build()
    }

}