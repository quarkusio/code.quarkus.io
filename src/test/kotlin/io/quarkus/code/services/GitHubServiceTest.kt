package io.quarkus.code.services

import io.quarkus.test.junit.QuarkusTest
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflySimulate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import javax.inject.Inject


@QuarkusTest
@HoverflySimulate
@ExtendWith(HoverflyExtension::class)
internal class GitHubServiceTest {

    @Inject
    lateinit var gitHubService: GitHubService

    @Test
    fun createAndPushRepository() {
        //given
        val path = Files.createTempDirectory("github-service-test")
        Files.copy(GitHubServiceTest::class.java.getResourceAsStream("/fakeextensions.json"), File(path.toString(), "test.json").toPath())

        //when
        val result = gitHubService.createRepository(GitHubServiceMock.TEST_TOKEN, "repo-name")
        gitHubService.push(result.first, GitHubServiceMock.TEST_TOKEN, result.second, path)
    }

    @Test
    fun fetchAccessToken() {
        gitHubService.fetchAccessToken(GitHubServiceMock.TEST_CODE, "shortRandomString")
    }
}