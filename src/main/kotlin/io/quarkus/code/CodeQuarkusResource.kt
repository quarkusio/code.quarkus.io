package io.quarkus.code

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.Config
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response


@Path("/")
class CodeQuarkusResource {

    @Inject
    internal lateinit var configManager: CodeQuarkusConfigManager

    @Inject
    internal lateinit var extensionCatalog: QuarkusExtensionCatalog

    @Inject
    internal lateinit var projectCreator: QuarkusProjectCreator

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    fun config(): Config {
        return configManager.getConfig()
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
        return Response
                .ok(projectCreator.create(project))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"${project.artifactId}.zip\"")
                .build()

    }
}