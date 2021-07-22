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
                && platformService.isLoaded
                && projectCreator != null) {
                    responseBuilder.withData("last updated", platformService.lastUpdated.toString())
                        .withData("number of extensions", "" + (platformService.recommendedCodeQuarkusExtensions.size))
                        .withData("recommended stream", platformService.recommendedStreamKey)
                        .withData("reload cron expr", config!!.quarkusPlatformReloadCronExpr)
                        .up()
        } else {
            responseBuilder.down()
        }
        return responseBuilder.build()
    }

}