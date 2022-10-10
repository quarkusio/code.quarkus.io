package io.quarkus.code.rest

import io.quarkus.code.config.PlatformConfig
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
    internal lateinit var platformConfig: PlatformConfig

    @Inject
    internal lateinit var platformService: PlatformService

    @Inject
    internal lateinit var projectCreator: QuarkusProjectService

    override fun call(): HealthCheckResponse {
        val responseBuilder = HealthCheckResponse.named("Code Quarkus HealthCheck")
        if(platformService.isLoaded) {
                    responseBuilder
                        .withData("cache last updated", platformService.cacheLastUpdated.toString())
                        .withData("registry timestamp", platformService.platformsCache.platformTimestamp)
                        .withData("recommended stream", platformService.recommendedStreamKey)
                        .withData("recommended stream quarkus core", platformService.recommendedPlatformInfo.quarkusCoreVersion)
                        .withData("recommended stream extensions",  platformService.recommendedCodeQuarkusExtensions.size.toString())
                        .withData("available streams", platformService.streamKeys.joinToString(", "))
                        .withData("reload cron expr", platformConfig.reloadCronExpr)
                        .withData("registryId", platformConfig.registryId.orElse("empty"))
                        .up()
        } else {
            responseBuilder.down()
        }
        return responseBuilder.build()
    }

}