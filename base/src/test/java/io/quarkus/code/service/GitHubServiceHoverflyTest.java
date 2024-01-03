package io.quarkus.code.service;

import io.quarkus.code.config.GitHubConfig;
import io.quarkus.code.model.GitHubToken;
import io.quarkus.code.service.GitHubServiceHoverflyTest.CustomSimulationPreprocessor;
import io.quarkus.test.junit.QuarkusTest;
import io.specto.hoverfly.junit.core.SimulationPreprocessor;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.common.Assert;

import jakarta.inject.Inject;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

/**
 * To record:
 * 1. Get a "code" using
 * https://github.com/login/oauth/authorize?client_id=e1177a88a6d9eec4bd16&scope=public_repo&state=lMqTg6m6A-wbzQsCv2qW8jW-y0Y
 * (it's in the query param of the redirection)
 * 2. Set the constant in the companion object CODE, LOGIN and AN_EXISTING_REPO
 * 4. Replace @HoverflySimulate by @HoverflyCapture
 * 5. Run all tests (should all pass)
 * 6. Replace @HoverflyCapture by @HoverflySimulate
 * 7. ----WARNING----- Replace your access_token in the recoding: io_quarkus_code_service_GitHubServiceTest.json
 */
@HoverflySimulate(config = @HoverflyConfig(destination = {
        "github.com" }, statefulCapture = true, simulationPreprocessor = CustomSimulationPreprocessor.class))
@ExtendWith(HoverflyExtension.class)
@QuarkusTest
public class GitHubServiceHoverflyTest {

    @Inject
    private GitHubService githubService;

    private GitHubToken token;

    private static final String STATE = "lMqTg6m6A-wbzQsCv2qW8jW-y0Y";

    // TO SET FOR CAPTURE (Read instruction at the top)
    private static final String CODE = "4bda4402b8a1c54771b6";
    private static final String LOGIN = "ia3andy";
    private static final String AN_EXISTING_REPO = "code.quarkus.io";
    private static final String REPO_TO_CREATE_NAME = "code.quarkus.io-testing-repo";

    public static <T> T newRestClientWithSSLContext(String baseUrl, SSLContext sslContext, Class<T> clazz)
            throws MalformedURLException {
        return RestClientBuilder.newBuilder()
                .sslContext(sslContext)
                .baseUrl(new URL(baseUrl))
                .build(clazz);
    }

    @BeforeEach
    public void setUp() throws IOException {
        token = githubService.fetchAccessToken(CODE, STATE);
        MatcherAssert.assertThat(token, Matchers.notNullValue());
        MatcherAssert.assertThat(token.accessToken(), Matchers.not(Matchers.emptyOrNullString()));
    }

    @Test
    public void shouldReturnTheUser() {
        MatcherAssert.assertThat(githubService.login(token.accessToken()), Matchers.is(LOGIN));
    }

    @Test
    public void shouldThrowExceptionWhenUsingInvalidCode() {
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> {
            githubService.fetchAccessToken("invalidcode", STATE);
        });
    }

    @Test
    public void createAndPushRepository() throws IOException {
        //given
        var path = Files.createTempDirectory("github-service-test");
        Files.copy(GitHubServiceHoverflyTest.class.getResourceAsStream("/fakeextensions.json"),
                new File(path.toString(), "test.json").toPath());

        //when
        var result = githubService.createRepository(LOGIN, token.accessToken(), REPO_TO_CREATE_NAME);
        MatcherAssert.assertThat(result.url(), Matchers.is("https://github.com/" + LOGIN + "/" + REPO_TO_CREATE_NAME + ".git"));
        MatcherAssert.assertThat(result.ownerName(), Matchers.is(LOGIN));
        githubService.push(result.ownerName(), token.accessToken(), "main", result.url(), path);
    }

    @Test
    public void shouldReturnTrueWhenRepositoryExists() {
        var exists = githubService.repositoryExists(LOGIN, token.accessToken(), AN_EXISTING_REPO);
        Assert.assertTrue(exists);
    }

    @Test
    public void shouldReturnFalseWhenRepositoryDoesNotExist() {
        var exists = githubService.repositoryExists(LOGIN, token.accessToken(), "new-repo-name");
        Assert.assertFalse(exists);
    }

    public record GitHubConfigImpl(Optional<String> clientId, Optional<String> clientSecret) implements GitHubConfig {
    }

    public static class CustomSimulationPreprocessor implements SimulationPreprocessor {
        @Override
        public void accept(Simulation simulation) {
            // Change the git-receive-pack matcher to a wildcard because the body will vary
            RequestFieldMatcher<String> receivePackBodyMatcher = simulation.getHoverflyData().getPairs().stream()
                    .filter(pair -> ((String) pair.getRequest().getPath().get(0).getValue()).contains("git-receive-pack"))
                    .findFirst()
                    .map(pair -> (RequestFieldMatcher<String>) pair.getRequest().getBody().get(0))
                    .orElseThrow();
            receivePackBodyMatcher.setMatcher(RequestFieldMatcher.MatcherType.GLOB);
            receivePackBodyMatcher.setValue("*");
        }
    }
}