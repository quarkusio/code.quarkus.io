package io.quarkus.code.github.model

data class TokenResponse(
        var access_token: String,
        var scope: String,
        var token_type: String
)