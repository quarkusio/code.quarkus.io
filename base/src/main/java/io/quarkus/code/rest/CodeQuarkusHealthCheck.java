package io.quarkus.code.rest;

import io.quarkus.code.config.PlatformConfig;
import io.quarkus.code.service.PlatformService;
import io.quarkus.code.service.QuarkusProjectService;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Readiness
@Singleton
public class CodeQuarkusHealthCheck implements HealthCheck {
    @Inject
    private PlatformConfig platformConfig;

    @Inject
    private PlatformService platformService;

    @Inject
    private QuarkusProjectService projectCreator;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Code Quarkus HealthCheck");
        if (platformService.isLoaded() && projectCreator != null) {
            responseBuilder
                    .withData("cache last updated", platformService.cacheLastUpdated().toString())
                    .withData("registry timestamp", platformService.platformsCache().platformTimestamp())
                    .withData("recommended stream", platformService.recommendedStreamKey())
                    .withData("recommended stream quarkus core", platformService.recommendedPlatformInfo().quarkusCoreVersion())
                    .withData("recommended stream extensions", String.valueOf(platformService.recommendedCodeQuarkusExtensions().size()))
                    .withData("available streams", String.join(", ", platformService.streamKeys()))
                    .withData("reload cron expr", platformConfig.getReloadCronExpr())
                    .withData("registryId", platformConfig.getRegistryId().orElse("empty"))
                    .up();
        } else {
            responseBuilder.down();
        }
        return responseBuilder.build();
    }
}