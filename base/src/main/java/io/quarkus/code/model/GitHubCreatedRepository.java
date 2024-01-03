package io.quarkus.code.model;

public record GitHubCreatedRepository(
        String ownerName,
        String url,
        String defaultBranch) {
}
