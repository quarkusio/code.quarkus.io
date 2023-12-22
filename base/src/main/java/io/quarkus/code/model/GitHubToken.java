package io.quarkus.code.model;

public record GitHubToken(
        String accessToken,
        String scope,
        String tokenType
){}