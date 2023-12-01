package io.quarkus.code.rest;

import io.quarkus.code.model.GitHubCreatedRepository;
import io.quarkus.code.model.GitHubToken;
import io.quarkus.code.service.GitHubService;

import java.nio.file.Path;

public class GitHubServiceMock extends GitHubService {

    private String existing = "existing-repo";

    @Override
    public String login(String token) {
        return "edewit";
    }

    @Override
    public boolean repositoryExists(String login, String token, String repositoryName) {
        return repositoryName.equals(existing);
    }

    @Override
    public GitHubCreatedRepository createRepository(String login, String token, String repositoryName) {
        existing = repositoryName;
        return new GitHubCreatedRepository("edewit", "https://github.com/edewit/" + repositoryName, "main");
    }

    @Override
    public void push(String ownerName, String token, String initialBranch, String httpTransportUrl, Path path) {
    }

    @Override
    public GitHubToken fetchAccessToken(String code, String state) {
        assert code.equals("gh-code");
        assert state.equals("someRandomState");
        return new GitHubToken("123", "", "");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}