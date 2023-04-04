package io.quarkus.code.rest

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.GitHubConfig
import io.quarkus.code.config.SegmentConfig
import io.quarkus.code.model.*
import io.quarkus.code.service.PlatformService
import io.quarkus.code.service.QuarkusProjectService
import io.quarkus.registry.catalog.PlatformCatalog
import io.quarkus.runtime.StartupEvent
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.eclipse.microprofile.context.ManagedExecutor
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.jboss.resteasy.reactive.NoCache
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response


@Path("/")
class CodeQuarkusResource @Inject constructor(
    val config: CodeQuarkusConfig,
    val segmentConfig: SegmentConfig,
    val gitHubConfig: GitHubConfig,
    val platformService: PlatformService,
    val projectCreator: QuarkusProjectService,
    val exec: ManagedExecutor
) {


    companion object {
        private val LOG = Logger.getLogger(CodeQuarkusResource::class.java.name)
        var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss' GMT'")
        private const val LAST_MODIFIED_HEADER = "Last-Modified"
    }

    fun onStart(@Observes e: StartupEvent) {
        config().subscribe().with {
            LOG.log(Level.INFO) {
                """
                    Code Quarkus is started with:
                        environment = ${it.environment}
                        sentryDSN = ${it.sentryDSN}
                        segmentWriteKey = ${segmentConfig.writeKeyForDisplay()}
                        quarkusPlatformVersion = ${it.quarkusPlatformVersion},
                        quarkusDevtoolsVersion = ${it.quarkusDevtoolsVersion},
                        gitCommitId: ${it.gitCommitId},
                        features: ${it.features}
                """.trimIndent()
            }
        }
    }

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @NoCache
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    fun config(): Uni<PublicConfig> {
        val publicConfig = PublicConfig(
            environment = config.environment.orElse("dev"),
            segmentWriteKey = segmentConfig.writeKey.filter(String::isNotBlank).orElse(null),
            sentryDSN = config.sentryFrontendDSN.filter(String::isNotBlank).orElse(null),
            quarkusPlatformVersion = config.quarkusPlatformVersion,
            quarkusDevtoolsVersion = config.quarkusDevtoolsVersion,
            quarkusVersion = config.quarkusPlatformVersion,
            gitCommitId = config.gitCommitId,
            gitHubClientId = gitHubConfig.clientId.filter(String::isNotBlank).orElse(null),
            features = config.features.map { listOf(it) }.orElse(listOf())
        )
        return Uni.createFrom().item(publicConfig)
    }

    @GET
    @Path("/platforms")
    @Produces(APPLICATION_JSON)
    @NoCache
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
    fun platforms(): Uni<Response> {
        val platformCatalog = platformService.platformCatalog
        val lastUpdated = platformService.cacheLastUpdated
        val response = Response.ok(platformCatalog).header(LAST_MODIFIED_HEADER, lastUpdated.format(formatter)).build()
        return Uni.createFrom().item(response)
    }

    @GET
    @Path("/streams")
    @Produces(APPLICATION_JSON)
    @NoCache
    @Operation(summary = "Get all available streams")
    @Tag(name = "Platform", description = "Platform related endpoints")
    @APIResponse(
        responseCode = "200",
        description = "All available streams",
        content = [Content(
            mediaType = APPLICATION_JSON,
            schema = Schema(implementation = Stream::class, type = SchemaType.ARRAY)
        )]
    )
    fun streams(): Uni<Response> {
        val streamKeys = platformService.streams
        val lastUpdated = platformService.cacheLastUpdated
        val response = Response.ok(streamKeys).header(LAST_MODIFIED_HEADER, lastUpdated.format(formatter)).build()
        return Uni.createFrom().item(response)
    }

    @GET
    @Path("/extensions")
    @Produces(APPLICATION_JSON)
    @NoCache
    @Operation(operationId = "extensions", summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @Tag(name = "Extensions", description = "Extension related endpoints")
    @APIResponse(
        responseCode = "200",
        description = "List of Quarkus extensions",
        content = [Content(
            mediaType = APPLICATION_JSON,
            schema = Schema(implementation = CodeQuarkusExtension::class, type = SchemaType.ARRAY)
        )]
    )
    fun extensions(
        @QueryParam("platformOnly") @DefaultValue("true") platformOnly: Boolean,
        @QueryParam("id") extensionId: String?
    ): Uni<Response>? {
        val extensions = platformService.recommendedCodeQuarkusExtensions
        return extensions(platformOnly, extensions, extensionId)
    }

    @GET
    @Path("/extensions/stream/{streamKey}")
    @Produces(APPLICATION_JSON)
    @NoCache
    @Operation(operationId = "extensionsForStream", summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @Tag(name = "Extensions", description = "Extension related endpoints")
    @APIResponse(
        responseCode = "200",
        description = "List of Quarkus extensions for a certain stream",
        content = [Content(
            mediaType = APPLICATION_JSON,
            schema = Schema(implementation = CodeQuarkusExtension::class, type = SchemaType.ARRAY)
        )]
    )
    fun extensionsForStream(
        @PathParam("streamKey") streamKey: String,
        @QueryParam("platformOnly") @DefaultValue("true") platformOnly: Boolean,
        @QueryParam("id") extensionId: String?
    ): Uni<Response>? {
        val extensions = platformService.getCodeQuarkusExtensions(streamKey)
        return extensions(platformOnly, extensions, extensionId)
    }

    private fun extensions(
        platformOnly: Boolean,
        extensions: List<CodeQuarkusExtension>,
        extensionId: String?
    ): Uni<Response>? {
        var extensionsFiltered = extensions
        if (platformOnly) {
            extensionsFiltered = extensionsFiltered.filter { it.platform }
        }
        if (extensionId != null) {
            extensionsFiltered = extensionsFiltered.filter { it.id == extensionId }
        }
        val lastUpdated = platformService.cacheLastUpdated
        val response = Response.ok(extensionsFiltered).header(LAST_MODIFIED_HEADER, lastUpdated.format(formatter)).build()
        return Uni.createFrom().item(response)
    }

    @POST
    @Path("/project")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Prepare a Quarkus application project to be downloaded")
    @Tag(name = "Project", description = "Project creation endpoints")
    fun project(@Valid projectDefinition: ProjectDefinition?): Uni<CreatedProject>? {
        val params = ArrayList<NameValuePair>()
        if (projectDefinition != null) {
            if (projectDefinition.streamKey != null) {
                params.add(BasicNameValuePair("S", projectDefinition.streamKey))
            }
            if (projectDefinition.groupId != ProjectDefinition.DEFAULT_GROUPID) {
                params.add(BasicNameValuePair("g", projectDefinition.groupId))
            }
            if (projectDefinition.artifactId != ProjectDefinition.DEFAULT_ARTIFACTID) {
                params.add(BasicNameValuePair("a", projectDefinition.artifactId))
            }
            if (projectDefinition.version != ProjectDefinition.DEFAULT_VERSION) {
                params.add(BasicNameValuePair("v", projectDefinition.version))
            }
            if (projectDefinition.buildTool != ProjectDefinition.DEFAULT_BUILDTOOL) {
                params.add(BasicNameValuePair("b", projectDefinition.buildTool))
            }
            if (projectDefinition.javaVersion != ProjectDefinition.DEFAULT_JAVA_VERSION) {
                params.add(BasicNameValuePair("j", projectDefinition.javaVersion))
            }
            if (projectDefinition.noCode != ProjectDefinition.DEFAULT_NO_CODE || projectDefinition.noExamples != ProjectDefinition.DEFAULT_NO_CODE) {
                params.add(BasicNameValuePair("nc", (!ProjectDefinition.DEFAULT_NO_CODE).toString()))
            }
            if (projectDefinition.extensions.isNotEmpty()) {
                projectDefinition.extensions.forEach { params.add(BasicNameValuePair("e", it)) }
            }
            if (projectDefinition.path != null) {
                params.add(BasicNameValuePair("p", projectDefinition.path))
            }
            if (projectDefinition.className != null) {
                params.add(BasicNameValuePair("c", projectDefinition.className))
            }
        }
        val path = if (params.isEmpty()) "/d" else "/d?${URLEncodedUtils.format(params, StandardCharsets.UTF_8)}"
        if (path.length > 1900) {
            throw BadRequestException(
                Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("The path is too long. Choose a sensible amount of extensions.")
                    .type(TEXT_PLAIN)
                    .build()
            )
        }
        return Uni.createFrom().item(CreatedProject(path))
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(operationId = "downloadForStream", summary = "Download a custom Quarkus application with the provided settings")
    @Tag(name = "Download", description = "Download endpoints")
    @Blocking
    fun getDownload(@Valid @BeanParam query: ProjectDefinitionQuery): Response {
        return download(query.toProjectDefinition())
    }

    @POST
    @Path("/download")
    @Consumes(APPLICATION_JSON)
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    @Tag(name = "Download", description = "Download endpoints")
    @Blocking
    fun download(@Valid projectDefinition: ProjectDefinition?): Response {
        val p = projectDefinition ?: ProjectDefinition()
        val platformInfo = platformService.getPlatformInfo(p.streamKey)
        return Response.ok(projectCreator.create(platformInfo, p))
            .type("application/zip")
            .header("Content-Disposition", "attachment; filename=\"${p.artifactId}.zip\"")
            .build()
    }
}
