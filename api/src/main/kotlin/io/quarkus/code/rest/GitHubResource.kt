package io.quarkus.code.rest

import io.quarkus.code.model.GitHubCreatedRepository
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.service.GitHubService
import io.quarkus.code.service.PlatformService
import io.quarkus.code.service.QuarkusProjectService
import io.quarkus.runtime.StartupEvent
import io.smallrye.common.annotation.Blocking
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.logging.Level
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response


@Path("/github")
@Blocking
class GitHubResource {

    companion object {
        private val LOG = Logger.getLogger(GitHubResource::class.java.name)
        private const val CHECK_CREATED_RETRY = 10
        private const val CHECK_CREATED_INTERVAL_FACTOR = 500L
    }

    @Inject
    internal lateinit var projectCreator: QuarkusProjectService

    @Inject
    internal lateinit var platformService: PlatformService

    @Inject
    lateinit var gitHubService: GitHubService

    fun onStart(@Observes e: StartupEvent) {
        if (gitHubService.isEnabled()) {
            LOG.log(Level.INFO) {
                """
            GitHub is enabled:
                clientId = ${gitHubService.config.clientId.orElse(null)}
                clientSecret = xxxxxxxxxx
            """.trimIndent()
            }
        } else {
            LOG.log(Level.INFO, "GitHub is disabled")
        }
    }

    @POST
    @Path("/project")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Create project and push generated code to GitHub")
    @Tag(name = "Project", description = "Project creation endpoints")
    fun createProject(
        @Valid projectDefinition: ProjectDefinition,
        @NotEmpty @HeaderParam("GitHub-Code") code: String,
        @NotEmpty @HeaderParam("GitHub-State") state: String
    ): GitHubCreatedRepository {
        check(gitHubService.isEnabled()) { "GitHub is not enabled" }
        val token = gitHubService.fetchAccessToken(code, state)
        val login = gitHubService.login(token.accessToken)
        if (gitHubService.repositoryExists(login, token.accessToken, projectDefinition.artifactId)) {
            throw WebApplicationException("This repository name ${projectDefinition.artifactId} already exists", 409)
        }
        val platformInfo = platformService.getPlatformInfo(projectDefinition.streamKey)
        val location = projectCreator.createTmp(platformInfo, projectDefinition, true)
        val repo = gitHubService.createRepository(login, token.accessToken, projectDefinition.artifactId)
        var created = false
        var i = 0
        while (!created && i < CHECK_CREATED_RETRY) {
            created = gitHubService.repositoryExists(login, token.accessToken, projectDefinition.artifactId)
            try {
                Thread.sleep(i * CHECK_CREATED_INTERVAL_FACTOR)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            ++i
            if (!created) {
                LOG.info("Repository not yet created retrying: $i/$CHECK_CREATED_RETRY")
            }
        }
        if (!created) {
            throw InternalServerErrorException(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while creating GitHub repository.")
                    .type(MediaType.TEXT_PLAIN)
                    .build()
            )
        }
        gitHubService.push(repo.ownerName, token.accessToken, repo.defaultBranch, repo.url, location)
        return repo
    }
}