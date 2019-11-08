package io.quarkus.code

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.code.services.GitHubService
import io.quarkus.code.services.QuarkusProjectCreator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.eclipse.microprofile.openapi.annotations.Operation
import java.io.IOException
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.Response

@Path("/github")
class GitHubResource {

    @Inject
    lateinit var gitHubService: GitHubService

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @Inject
    lateinit var configManager: CodeQuarkusConfigManager

    var client: OkHttpClient = OkHttpClient()

    @GET
    @Path("/push")
    @Operation(summary = "Push generated code to GitHub")
    fun pushCode(@Valid @BeanParam project: QuarkusProject, @QueryParam("token") token: String): Response {
        val location = projectCreator.createTmp(project)
        val repo = gitHubService.createRepository(token, project.artifactId)
        gitHubService.push(token, repo, location)
        return Response.ok(repo.fullName).build()
    }

    @GET
    @Path("/auth")
    fun authenticate(@QueryParam("code") code: String, @QueryParam("state") state: String): Response {
        val token = fetchAccessToken(code, state)
        return Response.ok(token).build()
    }

    private fun fetchAccessToken(code: String, state: String): String {
        val node = ObjectMapper().createObjectNode()
                .put("client_id", configManager.clientId)
                .put("client_secret", configManager.clientSecret)
                .put("state", state)
                .put("code", code)
        val request = Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(node.toString().toRequestBody("application/json".toMediaType())).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return response.body!!.string()
        }
    }
}