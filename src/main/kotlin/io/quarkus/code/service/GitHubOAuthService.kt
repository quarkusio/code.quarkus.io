@file:JvmName("io.quarkus.code.services.GitHubApi")
package io.quarkus.code.service

import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.net.URL
import javax.net.ssl.SSLContext
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap

@Path("/login/oauth")
@RegisterRestClient
interface GitHubOAuthService {

    companion object {
        fun newGitHubOAuthService(sslContext: SSLContext? = null): GitHubOAuthService {
            val builder = RestClientBuilder.newBuilder()
                    .baseUrl(URL("https://github.com"))
            if(sslContext != null) {
                builder.sslContext(sslContext)
            }
            return builder.build(GitHubOAuthService::class.java)
        }
    }

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

