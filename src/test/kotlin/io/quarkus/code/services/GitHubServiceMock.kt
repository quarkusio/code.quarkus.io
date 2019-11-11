package io.quarkus.code.services

import io.quarkus.test.Mock
import java.nio.file.Path
import javax.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
open class GitHubServiceMock: GitHubService() {

    companion object {
        const val TEST_TOKEN = "599256c4309d494b9d1c7ed3c5cbb1c13c91254c"
        const val TEST_CODE = "e12996e3cd2fc129264a"
    }
    override fun createRepository(token: String, repositoryName: String): Pair<String, String> {
        if (token == TEST_TOKEN) super.createRepository(token, repositoryName)
        assert(token == "123")
        return Pair("https://github.com/edewit/$repositoryName", "edewit")
    }

    override fun push(token: String, httpTransportUrl: String, ownerName: String, path: Path) {
        if (token == TEST_TOKEN) super.push(token, httpTransportUrl, ownerName, path)
    }

    override fun fetchAccessToken(code: String, state: String): String {
        if (code == TEST_CODE) super.fetchAccessToken(code, state)
        return "AccessToken"
    }
}