package io.quarkus.code

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.GoogleAnalyticsConfig
import io.quarkus.code.extensions.QuarkusExtensionCatalog
import io.quarkus.code.gen.QuarkusProjectCreator
import io.quarkus.code.github.GitHubService
import io.quarkus.code.model.*
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
import javax.ws.rs.*
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

    @Inject
    lateinit var gitHubService: GitHubService

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
                features = config.features.map { listOf(it) }.orElse(listOf())
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

    @POST
    @Path("/github/push")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Push generated code to GitHub")
    fun pushCode(@Valid @BeanParam project: QuarkusProject, @HeaderParam("token") token: String): GitHubCreatedRepository {
        check(gitHubService.isEnabled()) { "GitHub is not enabled" }
        val location = projectCreator.createTmp(project)
        val repo = gitHubService.createRepository(token, project.artifactId)
        gitHubService.push(repo.ownerName, token, repo.url, location)
        return repo
    }

    @GET
    @Path("/github/token")
    @Produces(APPLICATION_JSON)
    fun authenticate(@QueryParam("code") code: String, @QueryParam("state") state: String): GitHubToken {
        return gitHubService.fetchAccessToken(code, state)
    }
}