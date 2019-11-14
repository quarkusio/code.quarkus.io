package io.quarkus.code.github

import io.quarkus.test.junit.QuarkusTest
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflySimulate
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

@QuarkusTest
@HoverflySimulate(config = HoverflyConfig(destination = "github.com"))
@ExtendWith(HoverflyExtension::class)
internal class GitHubServiceTest {

    @Inject
    @field: RestClient
    internal lateinit var authService: GitHubOAuthService

    private val gitHubService = GitHubService()

    @BeforeEach
    fun initService() {
        gitHubService.authService = authService
        gitHubService.config = GitHubConfig("The client id", "The client secret")
    }

    @Test
    fun createAndPushRepository() {
        //given
        val path = Files.createTempDirectory("github-service-test")
        Files.copy(GitHubServiceTest::class.java.getResourceAsStream("/fakeextensions.json"), File(path.toString(), "test.json").toPath())

        //when
        val result = gitHubService.createRepository("test-token", "repo-name")
        assertThat(result.url, `is`("https://github.com/edewit/repo-name.git"))
        assertThat(result.ownerName, `is`("edewit"))
        gitHubService.push(result.ownerName, "test-token", result.url, path)
    }

    @Test
    fun fetchAccessToken() {
        val token = gitHubService.fetchAccessToken("e7d2998d567533b24fb8", "shortRandomString")
        assertThat(token.accessToken, `is`("b8410f0d46ab49b237000e4646c33fb7b193182a"))
    }
}