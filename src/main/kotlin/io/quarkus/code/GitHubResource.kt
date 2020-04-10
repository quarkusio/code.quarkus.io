package io.quarkus.code

import io.quarkus.code.gen.QuarkusProjectCreator
import io.quarkus.code.github.GitHubService
import io.quarkus.code.model.GitHubCreatedRepository
import io.quarkus.code.model.GitHubToken
import io.quarkus.code.model.QuarkusProject
import org.eclipse.microprofile.openapi.annotations.Operation
import java.util.logging.Logger
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType.APPLICATION_JSON


@Path("/github")
class GitHubResource {

    companion object {
        private val LOG = Logger.getLogger(GitHubResource::class.java.name)
    }

    @Inject
    internal lateinit var projectCreator: QuarkusProjectCreator

    @Inject
    lateinit var gitHubService: GitHubService

    @POST
    @Path("/project")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Push generated code to GitHub")
    fun createProject(@Valid @BeanParam project: QuarkusProject, @HeaderParam("token") token: String): GitHubCreatedRepository {
        check(gitHubService.isEnabled()) { "GitHub is not enabled" }
        val location = projectCreator.createTmp(project)
        val repo = gitHubService.createRepository(token, project.artifactId)
        gitHubService.push(repo.ownerName, token, repo.url, location)
        return repo
    }

    @GET
    @Path("/token")
    @Produces(APPLICATION_JSON)
    fun authenticate(@QueryParam("code") code: String, @QueryParam("state") state: String): GitHubToken {
        return gitHubService.fetchAccessToken(code, state)
    }
}