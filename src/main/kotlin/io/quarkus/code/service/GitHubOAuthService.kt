@file:JvmName("io.quarkus.code.services.GitHubApi")
package io.quarkus.code.service

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap

@Path("/login/oauth")
@RegisterRestClient(baseUri = "https://github.com")
interface GitHubOAuthService {

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

