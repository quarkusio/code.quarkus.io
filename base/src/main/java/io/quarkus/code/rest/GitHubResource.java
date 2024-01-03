package io.quarkus.code.rest;

import io.quarkus.code.config.GitHubConfig;
import io.quarkus.code.model.GitHubCreatedRepository;
import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.code.service.GitHubService;
import io.quarkus.code.service.PlatformService;
import io.quarkus.code.service.QuarkusProjectService;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/github")
@Blocking
public class GitHubResource {

    private static final Logger LOG = Logger.getLogger(GitHubResource.class.getName());
    private static final int CHECK_CREATED_RETRY = 10;
    private static final long CHECK_CREATED_INTERVAL_FACTOR = 500L;

    @Inject
    private QuarkusProjectService projectCreator;

    @Inject
    private PlatformService platformService;

    @Inject
    private GitHubService gitHubService;

    @Inject
    GitHubConfig config;

    public void onStart(@Observes StartupEvent e) {
        if (gitHubService.isEnabled()) {
            LOG.log(Level.INFO, () -> "GitHub is enabled:\n" +
                    "clientId = " + config.clientId().orElse(null) + "\n" +
                    "clientSecret = xxxxxxxxxx");
        } else {
            LOG.log(Level.INFO, "GitHub is disabled");
        }
    }

    @POST
    @Path("/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create project and push generated code to GitHub")
    @Tag(name = "Project", description = "Project creation endpoints")
    public GitHubCreatedRepository createProject(
            @Valid ProjectDefinition projectDefinition,
            @NotEmpty @HeaderParam("GitHub-Code") String code,
            @NotEmpty @HeaderParam("GitHub-State") String state) throws IOException, QuarkusCommandException {
        if (!gitHubService.isEnabled()) {
            throw new WebApplicationException("GitHub is not enabled", Response.Status.BAD_REQUEST);
        }
        var token = gitHubService.fetchAccessToken(code, state);
        var login = gitHubService.login(token.accessToken());
        if (gitHubService.repositoryExists(login, token.accessToken(), projectDefinition.artifactId())) {
            throw new WebApplicationException("This repository name " + projectDefinition.artifactId() + " already exists",
                    Response.Status.CONFLICT);
        }
        var platformInfo = platformService.platformInfo(projectDefinition.streamKey());
        var location = projectCreator.createTmp(platformInfo, projectDefinition, true);
        var repo = gitHubService.createRepository(login, token.accessToken(), projectDefinition.artifactId());
        var created = false;
        var i = 0;
        while (!created && i < CHECK_CREATED_RETRY) {
            created = gitHubService.repositoryExists(login, token.accessToken(), projectDefinition.artifactId());
            try {
                Thread.sleep(i * CHECK_CREATED_INTERVAL_FACTOR);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ++i;
            if (!created) {
                LOG.info("Repository not yet created retrying: " + i + "/" + CHECK_CREATED_RETRY);
            }
        }
        if (!created) {
            throw new InternalServerErrorException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error while creating GitHub repository.")
                            .type(MediaType.TEXT_PLAIN)
                            .build());
        }
        gitHubService.push(repo.ownerName(), token.accessToken(), repo.defaultBranch(), repo.url(), location);
        return repo;
    }
}