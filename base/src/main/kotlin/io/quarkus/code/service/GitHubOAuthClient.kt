@file:JvmName("io.quarkus.code.services.GitHubApi")
package io.quarkus.code.service

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MultivaluedMap

@Path("/login/oauth")
@RegisterRestClient(baseUri = "https://github.com")
interface GitHubOAuthClient {

    @POST
    @Path("/access_token")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    fun getAccessToken(tokenParameter: TokenParameter): MultivaluedMap<String, String>

    data class TokenParameter(
            var client_id: String,
            var client_secret: String,
            var code: String,
            var state: String
    )
}

