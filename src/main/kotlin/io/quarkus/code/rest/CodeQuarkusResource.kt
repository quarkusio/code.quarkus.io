package io.quarkus.code.rest

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.GitHubConfig
import io.quarkus.code.config.GoogleAnalyticsConfig
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.CreatedProject
import io.quarkus.code.model.PublicConfig
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.service.PlatformService
import io.quarkus.code.service.QuarkusProjectService
import io.quarkus.registry.catalog.PlatformCatalog
import io.quarkus.runtime.StartupEvent
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.lang.IllegalArgumentException
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.MediaType.TEXT_PLAIN
import javax.ws.rs.core.Response


@Path("/")
class CodeQuarkusResource {

    companion object {
        private val LOG = Logger.getLogger(CodeQuarkusResource::class.java.name)
        var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss' GMT'")
        private const val LAST_MODIFIED_HEADER = "Last-Modified"
    }

    @Inject
    internal lateinit var config: CodeQuarkusConfig

    @Inject
    lateinit var gaConfig: GoogleAnalyticsConfig

    @Inject
    lateinit var gitHubConfig: GitHubConfig

    @Inject
    internal lateinit var platformService: PlatformService

    @Inject
    internal lateinit var projectCreator: QuarkusProjectService

    fun onStart(@Observes e: StartupEvent) {
        LOG.log(Level.INFO) {"""
            Code Quarkus is started with:
                environment = ${config().environment}
                sentryDSN = ${config().sentryDSN}
                quarkusVersion = ${config().quarkusVersion},
                gitCommitId: ${config().gitCommitId},
                features: ${config().features}
        """.trimIndent()}
    }

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    fun config(): PublicConfig {
        return PublicConfig(
                environment = config.environment.orElse("dev"),
                gaTrackingId = gaConfig.trackingId.filter(String::isNotBlank).orElse(null),
                sentryDSN = config.sentryDSN.filter(String::isNotBlank).orElse(null),
                quarkusVersion = config.quarkusVersion,
                gitCommitId = config.gitCommitId,
                gitHubClientId = gitHubConfig.clientId.filter(String::isNotBlank).orElse(null),
                features = config.features.map { listOf(it) }.orElse(listOf())
        )
    }

    @GET
    @Path("/platforms")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get all available platforms")
    @Tag(name = "Platform", description = "Platform related endpoints")
    @APIResponse(
        responseCode = "200",
        description = "All available platforms",
        content = [Content(
            mediaType = APPLICATION_JSON,
            schema = Schema(implementation = PlatformCatalog::class)
        )]
    )
    fun platforms(): Response {
        val platformCatalog = platformService.platformCatalog
        val lastUpdated = platformService.lastUpdated
        return Response.ok(platformCatalog).header(LAST_MODIFIED_HEADER,lastUpdated?.format(formatter)).build()
    }

    @GET
    @Path("/keys/stream")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get all available stream keys")
    @Tag(name = "Platform", description = "Platform related endpoints")
    @APIResponse(
        responseCode = "200",
        description = "All available stream keys",
        content = [Content(
            mediaType = APPLICATION_JSON
        )]
    )
    fun streamKeys(): Response {
        val streamKeys = platformService.streamKeys
        val lastUpdated = platformService.lastUpdated
        return Response.ok(streamKeys).header(LAST_MODIFIED_HEADER,lastUpdated?.format(formatter)).build()
    }

    @GET
    @Path("/extensions")
    @Produces(APPLICATION_JSON)
    @Operation(operationId="extensions", summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @Tag(name = "Extensions", description = "Extension related endpoints")
    @APIResponse(
            responseCode = "200",
            description = "List of Quarkus extensions",
            content = [Content(
                    mediaType = APPLICATION_JSON,
                    schema = Schema(implementation = CodeQuarkusExtension::class, type = SchemaType.ARRAY)
            )]
    )
    fun extensions(@QueryParam("platformOnly") @DefaultValue("true") platformOnly: Boolean): Response {
        var extensions = platformService.extensionCatalog
        if(platformOnly){
            extensions = extensions?.filter { it.platform }
        }
        val lastUpdated = platformService.lastUpdated
        return Response.ok(extensions).header(LAST_MODIFIED_HEADER,lastUpdated?.format(formatter)).build()
    }

    @GET
    @Path("/extensions/stream/{stream}")
    @Produces(APPLICATION_JSON)
    @Operation(operationId="extensionsForStream", summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @Tag(name = "Extensions", description = "Extension related endpoints")
    @APIResponse(
        responseCode = "200",
        description = "List of Quarkus extensions for a certain stream",
        content = [Content(
            mediaType = APPLICATION_JSON,
            schema = Schema(implementation = CodeQuarkusExtension::class, type = SchemaType.ARRAY)
        )]
    )
    fun extensionsForStream(@PathParam("stream") stream: String, @QueryParam("platformOnly") @DefaultValue("true") platformOnly: Boolean): Response {
        var extensions = platformService.getExtensionCatalogForStream(stream)
        if(platformOnly){
            extensions = extensions?.filter { it.platform }
        }
        val lastUpdated = platformService.lastUpdated
        return Response.ok(extensions).header(LAST_MODIFIED_HEADER, lastUpdated?.format(formatter)).build();
    }

    @POST
    @Path("/project")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Prepare a Quarkus application project to be downloaded")
    @Tag(name = "Project", description = "Project creation endpoints")
    fun project(@Valid projectDefinition: ProjectDefinition?): CreatedProject {
        val params = ArrayList<NameValuePair>()
        if (projectDefinition != null) {
            if(projectDefinition.groupId != ProjectDefinition.DEFAULT_GROUPID) {
                params.add(BasicNameValuePair("g", projectDefinition.groupId))
            }
            if(projectDefinition.artifactId != ProjectDefinition.DEFAULT_ARTIFACTID) {
                params.add(BasicNameValuePair("a", projectDefinition.artifactId))
            }
            if(projectDefinition.version != ProjectDefinition.DEFAULT_VERSION) {
                params.add(BasicNameValuePair("v", projectDefinition.version))
            }
            if(projectDefinition.buildTool != ProjectDefinition.DEFAULT_BUILDTOOL) {
                params.add(BasicNameValuePair("b", projectDefinition.buildTool))
            }
            if(projectDefinition.noCode != ProjectDefinition.DEFAULT_NO_CODE || projectDefinition.noExamples != ProjectDefinition.DEFAULT_NO_CODE) {
                params.add(BasicNameValuePair("nc", projectDefinition.noCode.toString()))
            }
            if(projectDefinition.extensions.isNotEmpty()) {
                projectDefinition.extensions.forEach { params.add(BasicNameValuePair("e", it)) }
            }
        }
        val path = if(params.isEmpty()) "/d" else "/d?${URLEncodedUtils.format(params, StandardCharsets.UTF_8)}"
       if (path.length > 1900) {
           throw BadRequestException(Response
               .status(Response.Status.BAD_REQUEST)
               .entity("The path is too long. Choose a sensible amount of extensions.")
               .type(TEXT_PLAIN)
               .build())
       }
        return CreatedProject(path)
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    @Tag(name = "Download", description = "Download endpoints")
    fun download(@Valid @BeanParam projectDefinition: ProjectDefinition): Response {
        try {
            return Response
                    .ok(projectCreator.create(projectDefinition))
                    .type("application/zip")
                    .header("Content-Disposition", "attachment; filename=\"${projectDefinition.artifactId}.zip\"")
                    .build()
        } catch (e: IllegalArgumentException) {
            val message = "Bad request: ${e.message}"
            LOG.warning(message)
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(message)
                .type(TEXT_PLAIN)
                .build()
        }
    }

    @POST
    @Path("/download")
    @Consumes(APPLICATION_JSON)
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    @Tag(name = "Download", description = "Download endpoints")
    fun postDownload(@Valid projectDefinition: ProjectDefinition?): Response {
        try {
            val project = projectDefinition ?: ProjectDefinition()
            return Response
                .ok(projectCreator.create(project))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"${project.artifactId}.zip\"")
                .build()
        } catch (e: IllegalArgumentException) {
            val message = "Bad request: ${e.message}"
            LOG.warning(message)
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(message)
                .type(TEXT_PLAIN)
                .build()
        }
    }
}