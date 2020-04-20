package io.quarkus.code.model

data class GitHubToken(
        var accessToken: String,
        var scope: String,
        var tokenType: String
)