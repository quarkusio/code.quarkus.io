package io.quarkus.code.rest

import io.quarkus.code.service.GitHubService
import io.quarkus.code.model.GitHubCreatedRepository
import io.quarkus.code.model.GitHubToken
import io.quarkus.test.Mock
import java.nio.file.Path
import javax.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
class GitHubServiceMock: GitHubService() {

    override fun login(token: String): String {
        return "edewit"
    }

    override fun repositoryExists(login: String, token: String, repositoryName: String): Boolean {
        return repositoryName == "existing-repo"
    }

    override fun createRepository(login: String, token: String, repositoryName: String): GitHubCreatedRepository {
        assert(token == "123")
        return GitHubCreatedRepository("edewit", "https://github.com/edewit/$repositoryName")
    }

    override fun push(ownerName: String, token: String, httpTransportUrl: String, path: Path) {
    }

    override fun fetchAccessToken(code: String, state: String): GitHubToken {
        assert(code == "gh-code")
        assert(state == "someRandomState")
        return GitHubToken("123", "", "")
    }

    override fun isEnabled(): Boolean {
        return true
    }
}