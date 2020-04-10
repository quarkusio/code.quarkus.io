package io.quarkus.code

import io.quarkus.code.github.GitHubService
import io.quarkus.code.model.GitHubCreatedRepository
import io.quarkus.code.model.GitHubToken
import io.quarkus.test.Mock
import java.nio.file.Path
import javax.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
class GitHubServiceMock: GitHubService() {

    override fun createRepository(token: String, repositoryName: String): GitHubCreatedRepository {
        assert(token == "123")
        return GitHubCreatedRepository("edewit", "https://github.com/edewit/$repositoryName")
    }

    override fun push(ownerName: String, token: String, httpTransportUrl: String, path: Path) {
    }

    override fun fetchAccessToken(code: String, state: String): GitHubToken {
        return GitHubToken("AccessToken", "", "")
    }

    override fun isEnabled(): Boolean {
        return true
    }
}