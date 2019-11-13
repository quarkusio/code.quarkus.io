package io.quarkus.code

import io.quarkus.code.github.GitHubService
import io.quarkus.code.github.model.CreatedRepository
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
    override fun createRepository(token: String, repositoryName: String): CreatedRepository {
        assert(token == "123")
        return CreatedRepository("edewit", "https://github.com/edewit/$repositoryName")
    }

    override fun push(ownerName: String, token: String, httpTransportUrl: String, path: Path) {
    }

    override fun fetchAccessToken(code: String, state: String): String {
        return "{ \"access_token\": \"AccessToken\"}"
    }
}