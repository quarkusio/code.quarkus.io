package io.quarkus.code

import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.GitHubService
import io.quarkus.code.services.QuarkusProjectCreator
import org.eclipse.microprofile.openapi.annotations.Operation
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response

@Path("/github")
class GitHubResource {

    @Inject
    lateinit var gitHubService: GitHubService

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @GET
    @Path("/push")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Push generated code to GitHub")
    fun pushCode(@Valid @BeanParam project: QuarkusProject, @HeaderParam("token") token: String): Response {
        val location = projectCreator.createTmp(project)
        val repo = gitHubService.createRepository(token, project.artifactId)
        gitHubService.push(repo.first, token, repo.second, location)
        return Response.ok("{ \"repository\": \"${repo.second}\"}").build()
    }

    @GET
    @Path("/auth")
    @Produces(APPLICATION_JSON)
    fun authenticate(@QueryParam("code") code: String, @QueryParam("state") state: String): Response {
        val token = gitHubService.fetchAccessToken(code, state)
        return Response.ok(token).build()
    }
}