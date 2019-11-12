package io.quarkus.code.model

data class TokenParameter(
        var client_id: String,
        var client_secret: String,
        var code: String,
        var state: String
)

data class AccessToken(
        var access_token: String = "",
        var token_type: String = "",
        var scope: String = ""
)