package io.quarkus.code

import io.quarkus.code.model.Config
import io.quarkus.code.model.QuarkusExtension
import io.quarkus.code.model.QuarkusProject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.metrics.annotation.Counted
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.io.IOException
import javax.inject.Inject
import javax.validation.constraints.NotEmpty
import javax.ws.rs.*
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response


@Path("/")
class CodeQuarkusResource {

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @ConfigProperty(name = "io.quarkus.code.quarkus-version")
    lateinit var quarkusVersion: String

    @ConfigProperty(name = "io.quarkus.code.git-commit-id")
    lateinit var gitCommitId: String

    @ConfigProperty(name = "io.quarkus.code.environment", defaultValue = "dev")
    lateinit var environment: String

    @ConfigProperty(name = "io.quarkus.code.ga-tracking-id", defaultValue = "")
    lateinit var gaTrackingId: String

    @ConfigProperty(name = "io.quarkus.code.sentry-dsn", defaultValue = "")
    lateinit var sentryDSN: String

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    fun config(): Config {
        return Config(
                environment,
                gaTrackingId,
                sentryDSN,
                quarkusVersion,
                gitCommitId
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
                    schema = Schema(implementation = QuarkusExtension::class)
            )]
    )
    @Counted(name = "countedExtensions", description = "How many time an application has been downloaded")
    fun extensions(): Response {
        val extensionsResource = CodeQuarkusResource::class.java.getResource("/quarkus/extensions.json")
                ?: throw IOException("missing extensions.json file")
        return Response
                .ok(extensionsResource.readBytes())
                .type(APPLICATION_JSON)
                .build()
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    fun download(
            @DefaultValue(QuarkusProject.DEFAULT_GROUPID)
            @NotEmpty
            @QueryParam("g")
            @Parameter(name = "g", description = "GAV: groupId (default: ${QuarkusProject.DEFAULT_GROUPID})", required = false)
            groupId: String,

            @DefaultValue(QuarkusProject.DEFAULT_ARTIFACTID)
            @NotEmpty
            @QueryParam("a")
            @Parameter(name = "a", description = "GAV: artifactId (default: ${QuarkusProject.DEFAULT_ARTIFACTID})", required = false)
            artifactId: String,

            @DefaultValue(QuarkusProject.DEFAULT_VERSION)
            @NotEmpty
            @QueryParam("v")
            @Parameter(name = "v", description = "GAV: version (default: ${QuarkusProject.DEFAULT_VERSION})", required = false)
            version: String,

            @DefaultValue(QuarkusProject.DEFAULT_CLASSNAME)
            @NotEmpty
            @QueryParam("c")
            @Parameter(name = "c", description = "The class name to use in the generated application (default: ${QuarkusProject.DEFAULT_CLASSNAME})", required = false)
            className: String,

            @DefaultValue(QuarkusProject.DEFAULT_PATH)
            @NotEmpty
            @QueryParam("p")
            @Parameter(name = "p", description = "The path of the REST endpoint created in the generated application (default: ${QuarkusProject.DEFAULT_PATH})", required = false)
            path: String,

            @QueryParam("e")
            @Parameter(name = "e", description = "The set of extension ids that will be included in the generated application", required = false)
            extensions: Set<String>
    ): Response {
        val project = QuarkusProject(groupId, artifactId, version, className, path, extensions)
        return Response
                .ok(projectCreator.create(project))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"$artifactId.zip\"")
                .build()

    }

}