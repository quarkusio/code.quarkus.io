package io.quarkus.code.github

import io.quarkus.code.GitHubServiceMock
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflySimulate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import java.io.File
import java.nio.file.Files


@HoverflySimulate(config = HoverflyConfig(destination = "github.com"))
@ExtendWith(HoverflyExtension::class)
internal class GitHubServiceTest {

    val gitHubService = GitHubService()

    @Test
    fun createAndPushRepository() {
        //given
        val path = Files.createTempDirectory("github-service-test")
        Files.copy(GitHubServiceTest::class.java.getResourceAsStream("/fakeextensions.json"), File(path.toString(), "test.json").toPath())

        //when
        val result = gitHubService.createRepository(GitHubServiceMock.TEST_TOKEN, "repo-name")
        assertThat(result.url, `is`("https://github.com/edewit/repo-name.git"))
        assertThat(result.ownerName, `is`("edewit"))
        gitHubService.push(result.ownerName, GitHubServiceMock.TEST_TOKEN, result.url, path)
    }

    @Test
    fun fetchAccessToken() {
        gitHubService.config = GitHubConfig("", "")
        gitHubService.authService = mock(GitHubOAuthService::class.java)
        gitHubService.fetchAccessToken(GitHubServiceMock.TEST_CODE, "shortRandomString")
    }
}