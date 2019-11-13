package io.quarkus.code.github.model

data class TokenParameter(
        var client_id: String,
        var client_secret: String,
        var code: String,
        var state: String
)