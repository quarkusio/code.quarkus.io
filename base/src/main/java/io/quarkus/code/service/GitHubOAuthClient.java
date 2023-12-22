package io.quarkus.code.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

@Path("/login/oauth")
@RegisterRestClient(baseUri = "https://github.com")
public interface GitHubOAuthClient {

    @POST
    @Path("/access_token")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    MultivaluedMap<String, String> getAccessToken(TokenParameter tokenParameter);

    record TokenParameter(
            String client_id,
            String client_secret,
            String code,
            String state
    ) {}
}
