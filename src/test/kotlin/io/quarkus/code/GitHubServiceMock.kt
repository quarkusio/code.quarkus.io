package io.quarkus.code

import io.quarkus.code.github.GitHubService
import io.quarkus.code.github.model.CreatedRepository
import io.quarkus.code.github.model.TokenResponse
import io.quarkus.test.Mock
import java.nio.file.Path
import javax.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
open class GitHubServiceMock: GitHubService() {

    override fun createRepository(token: String, repositoryName: String): CreatedRepository {
        assert(token == "123")
        return CreatedRepository("edewit", "https://github.com/edewit/$repositoryName")
    }

    override fun push(ownerName: String, token: String, httpTransportUrl: String, path: Path) {
    }

    override fun fetchAccessToken(code: String, state: String): TokenResponse {
        return TokenResponse("AccessToken", "", "")
    }
}