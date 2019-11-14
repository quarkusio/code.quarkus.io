package io.quarkus.code.github

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.inject.Singleton

@Singleton
open class GitHubConfig() {

    @ConfigProperty(name = "io.quarkus.code.github.clientId", defaultValue = "")
    lateinit var clientId: String
        private set

    @ConfigProperty(name = "io.quarkus.code.github.clientSecret", defaultValue = "")
    lateinit var clientSecret: String
        private set

    constructor(clientId: String, clientSecret: String) : this() {
        this.clientId = clientId
        this.clientSecret = clientSecret
    }

    fun isGitHubEnabled() = this.clientId.isNotEmpty() && this.clientSecret.isNotEmpty()
}