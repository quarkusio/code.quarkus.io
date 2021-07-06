package io.quarkus.code.rest

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.service.PlatformService
import io.quarkus.code.service.QuarkusProjectService
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
    internal lateinit var platformService: PlatformService

    @Inject
    internal var projectCreator: QuarkusProjectService? = null

    override fun call(): HealthCheckResponse {
        val responseBuilder = HealthCheckResponse.named("Code Quarkus HealthCheck")
        if(config != null
                && !platformService?.extensionCatalog.isNullOrEmpty()
                && projectCreator != null) {
                    responseBuilder.withData("last updated", platformService.lastUpdated.toString())
                        .withData("number of extensions", "" + (platformService?.extensionCatalog?.size ?: 0))
                        .withData("default stream", platformService.getDefaultStreamKey())
                        .up()
        } else {
            responseBuilder.down()
        }
        return responseBuilder.build()
    }

}