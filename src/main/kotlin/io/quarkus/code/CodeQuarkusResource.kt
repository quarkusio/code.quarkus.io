package io.quarkus.code

import io.quarkus.code.analytics.GoogleAnalyticsService
import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.GoogleAnalyticsConfig
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.Config
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.util.logging.Level
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.MediaType.TEXT_PLAIN
import javax.ws.rs.core.Response


@Path("/")
class CodeQuarkusResource {

    companion object {
        private val LOG = Logger.getLogger(CodeQuarkusResource::class.java.name)
    }

    @Inject
    internal lateinit var config: CodeQuarkusConfig

    @Inject
    lateinit var gaConfig: GoogleAnalyticsConfig

    @Inject
    internal lateinit var extensionCatalog: QuarkusExtensionCatalog

    @Inject
    internal lateinit var projectCreator: QuarkusProjectCreator

    fun onStart(@Observes e: StartupEvent) {
        LOG.log(Level.INFO) {"""
            Code Quarkus is started with:
                environment = ${config.environment}
                sentryDSN = ${config.sentryDSN.filter(String::isNotBlank).orElse(null)}
                quarkusVersion = ${config.quarkusVersion},
                gitCommitId: ${config.gitCommitId}
        """.trimIndent()}
    }

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    fun config(): Config {
        return Config(
                environment = config.environment,
                gaTrackingId = gaConfig.trackingId.filter(String::isNotBlank).orElse(null),
                sentryDSN = config.sentryDSN.filter(String::isNotBlank).orElse(null),
                quarkusVersion = config.quarkusVersion,
                gitCommitId = config.gitCommitId
        )
    }


    @GET
    @Path("/extensions")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @APIResponse(
            responseCode = "200",
            description = "List of Quarkus extensions",
            content = [Content(
                    mediaType = APPLICATION_JSON,
                    schema = Schema(implementation = CodeQuarkusExtension::class)
            )]
    )
    fun extensions(): List<CodeQuarkusExtension> {
        return extensionCatalog.extensions
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    fun download(@Valid @BeanParam project: QuarkusProject): Response {
        try {
            return Response
                    .ok(projectCreator.create(project))
                    .type("application/zip")
                    .header("Content-Disposition", "attachment; filename=\"${project.artifactId}.zip\"")
                    .build()
        } catch (e: IllegalStateException) {
            LOG.warning("Bad request: ${e.message}")
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .type(TEXT_PLAIN)
                    .build()
        }
    }
}