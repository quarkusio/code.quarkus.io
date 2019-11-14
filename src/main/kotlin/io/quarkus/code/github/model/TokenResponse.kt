package io.quarkus.code.github.model

data class TokenResponse(
        var accessToken: String,
        var scope: String,
        var tokenType: String
)