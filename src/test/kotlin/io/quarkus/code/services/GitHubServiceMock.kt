package io.quarkus.code.services

import io.quarkus.code.model.AccessToken
import io.quarkus.test.Mock
import java.nio.file.Path
import javax.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
open class GitHubServiceMock: GitHubService() {

    companion object {
        const val TEST_TOKEN = "test-token"
        const val TEST_CODE = "e7d2998d567533b24fb8"
    }
    override fun createRepository(token: String, repositoryName: String): Pair<String, String> {
        if (token == TEST_TOKEN) return super.createRepository(token, repositoryName)
        assert(token == "123")
        return Pair("edewit", "https://github.com/edewit/$repositoryName")
    }

    override fun push(ownerName: String, token: String, httpTransportUrl: String, path: Path) {
        if (token == TEST_TOKEN) super.push(ownerName, token, httpTransportUrl, path)
    }

    override fun fetchAccessToken(code: String, state: String): AccessToken {
        if (code == TEST_CODE) return super.fetchAccessToken(code, state)
        return AccessToken("AccessToken")
    }
}