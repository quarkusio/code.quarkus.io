package io.launcher.quarkus

import io.launcher.quarkus.model.Config
import io.launcher.quarkus.model.QuarkusExtension
import io.launcher.quarkus.model.QuarkusProject
import org.eclipse.microprofile.metrics.annotation.Counted
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.io.IOException
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType.APPLICATION_JSON
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
            System.getenv("ENV") ?: "dev",
            System.getenv("GA_TRACKING_ID") ?: null,
            System.getenv("SENTRY_DSN") ?: null
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
    @Counted(name = "countedExtensions", description = "How many time an application has been downloaded")
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
    fun download(

        @QueryParam("g")
        @Parameter(name= "g", description = "GAV: groupId (default: ${QuarkusProject.DEFAULT_GROUPID})", required = false)
        @DefaultValue(QuarkusProject.DEFAULT_GROUPID)
        groupId: String,

        @QueryParam("a")
        @Parameter(name= "a", description = "GAV: artifactId (default: ${QuarkusProject.DEFAULT_ARTIFACTID})", required = false)
        @DefaultValue(QuarkusProject.DEFAULT_ARTIFACTID)
        artifactId: String,

        @QueryParam("v")
        @Parameter(name= "v", description = "GAV: version (default: ${QuarkusProject.DEFAULT_VERSION})", required = false)
        @DefaultValue(QuarkusProject.DEFAULT_VERSION)
        version: String,


        @QueryParam("c")
        @Parameter(name= "c", description = "The class name to use in the generated application (default: ${QuarkusProject.DEFAULT_CLASSNAME})", required = false)
        @DefaultValue(QuarkusProject.DEFAULT_CLASSNAME)
        className: String,

        @QueryParam("p")
        @Parameter(name= "p", description = "The path of the REST endpoint created in the generated application (default: ${QuarkusProject.DEFAULT_GROUPID})", required = false)
        @DefaultValue(QuarkusProject.DEFAULT_PATH)
        path: String,

        @QueryParam("e")
        @Parameter(name= "e", description = "The set of extension ids that will be included in the generated application", required = false)
        extensions: Set<String>
    ): Response {
        return Response
            .ok(projectCreator.create(QuarkusProject(groupId, artifactId, version, className, path, extensions)))
            .type("application/zip")
            .header("Content-Disposition", "attachment; filename=\"$artifactId.zip\"")
            .build()

    }

}