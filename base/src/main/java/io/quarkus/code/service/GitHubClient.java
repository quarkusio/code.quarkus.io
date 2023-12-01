package io.quarkus.code.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "https://api.github.com")
public interface GitHubClient {

    static String toAuthorization(String token) {
        return "token " + token;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    GHMe getMe(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/repos/{ownerName}/{repoName}")
    @Produces(MediaType.APPLICATION_JSON)
    GHRepo getRepo(@HeaderParam("Authorization") String authorization,
                   @PathParam("ownerName") String ownerName,
                   @PathParam("repoName") String repoName);

    @POST
    @Path("/user/repos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    GHRepo createRepo(@HeaderParam("Authorization") String authorization,
                      GHCreateRepo repo);

    @JsonIgnoreProperties(ignoreUnknown = true)
    class GHMe {
        private String login;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class GHRepo {
        private String name;
        private String description;
        @JsonProperty("clone_url")
        private String cloneUrl;
        @JsonProperty("default_branch")
        private String defaultBranch;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCloneUrl() {
            return cloneUrl;
        }

        public void setCloneUrl(String cloneUrl) {
            this.cloneUrl = cloneUrl;
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }
    }

    class GHCreateRepo {
        private String name;
        private String description;

        public GHCreateRepo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
