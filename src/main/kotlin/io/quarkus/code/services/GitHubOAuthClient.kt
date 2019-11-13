@file:JvmName("io.quarkus.code.services.GitHubApi")
package io.quarkus.code.services

import io.quarkus.code.model.TokenParameter
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/login/oauth")
@RegisterRestClient
interface GitHubOAuthClient {

    @POST
    @Path("/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAccessToken(tokenParameter: TokenParameter): String
}

