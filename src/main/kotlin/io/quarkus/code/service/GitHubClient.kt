package io.quarkus.code.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@RegisterRestClient(baseUri = "https://api.github.com")
interface GitHubClient {

    companion object {
        fun toAuthorization(token: String) = "token $token"
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    fun getMe(@HeaderParam("Authorization") authorization: String): GHMe

    @GET
    @Path("/repos/{ownerName}/{repoName}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getRepo(@HeaderParam("Authorization") authorization: String,
                      @PathParam("ownerName") ownerName: String,
                      @PathParam("repoName") repoName: String): GHRepo

    @POST
    @Path("/user/repos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createRepo(@HeaderParam("Authorization") authorization: String,
                   repo: GHCreateRepo): GHRepo

    @JsonIgnoreProperties(ignoreUnknown = true)
    class GHMe {
        lateinit var login: String
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class GHRepo {
        lateinit var name: String
        var description: String? = null

        @JsonProperty("clone_url")
        lateinit var cloneUrl: String
    }

    data class GHCreateRepo(val name: String, val description: String)

}

