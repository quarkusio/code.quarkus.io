package io.quarkus.code.health

import io.quarkus.code.services.QuarkusExtensionCatalog
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@Readiness
@ApplicationScoped
open class QuarkusExtensionCatalogHealthCheck : HealthCheck {

    @Inject
    lateinit var extensionCatalog: QuarkusExtensionCatalog

    override fun call(): HealthCheckResponse {
        return HealthCheckResponse.named("Extension catalog health check")
                .state(extensionCatalog.isLoaded())
                .build()
    }
}