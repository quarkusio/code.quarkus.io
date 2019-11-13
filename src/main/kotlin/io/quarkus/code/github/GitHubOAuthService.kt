@file:JvmName("io.quarkus.code.services.GitHubApi")
package io.quarkus.code.github

import io.quarkus.code.github.model.TokenParameter
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/login/oauth")
@RegisterRestClient(baseUri = "https://github.com/")
interface GitHubOAuthService {

    @POST
    @Path("/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAccessToken(tokenParameter: TokenParameter): String
}

