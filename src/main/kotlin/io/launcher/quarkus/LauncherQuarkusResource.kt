package io.launcher.quarkus

import io.launcher.quarkus.model.Config
import io.launcher.quarkus.model.QuarkusExtension
import io.launcher.quarkus.model.QuarkusProject
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.io.IOException
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.*
import javax.ws.rs.core.Response


@Path("/quarkus")
class LauncherQuarkusResource {

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    fun config(): Config {
        return Config(
            System.getenv("LAUNCHER_QUARKUS_ENV") ?: "dev",
            System.getenv("LAUNCHER_QUARKUS_GA_TRACKING_ID") ?: null,
            System.getenv("LAUNCHER_QUARKUS_SENTRY_DSN") ?: null
        )
    }

    @GET
    @Path("/extensions")
    @Produces(APPLICATION_JSON)
    @Operation(
        summary = "Get the Quarkus Launcher list of Quarkus extensions"
    )
    @APIResponse(
        responseCode = "200",
        description = "List of Quarkus extensions",
        content = [Content(
            mediaType = APPLICATION_JSON,
            schema = Schema(implementation = QuarkusExtension::class)
        )]
    )
    fun extensions(): Response {
        return Response
            .ok(
                (LauncherQuarkusResource::class.java.getResource("/quarkus/extensions.json")
                    ?: throw IOException("missing extensions.json file")).readBytes()
            )
            .type(APPLICATION_JSON)
            .build()
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    fun download(@Valid @BeanParam params: QuarkusProject): Response {
        return Response
            .ok(projectCreator.create(params))
            .type("application/zip")
            .header("Content-Disposition", "attachment; filename=\"${params.artifactId}.zip\"")
            .build()

    }

}