package io.quarkus.code.services

import io.quarkus.test.junit.QuarkusTest
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyCapture
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflySimulate
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import javax.inject.Inject


@QuarkusTest
//@HoverflySimulate
@HoverflyCapture(config = HoverflyConfig(statefulCapture = true))
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
        val repository = gitHubService.createRepository(GitHubServiceMock.TEST_TOKEN, "repo-name")
        gitHubService.push(GitHubServiceMock.TEST_TOKEN, repository.first, repository.second, path)
    }

    @Test
    fun fetchAccessToken() {
        gitHubService.fetchAccessToken(GitHubServiceMock.TEST_CODE, "shortRandomString");
    }
}