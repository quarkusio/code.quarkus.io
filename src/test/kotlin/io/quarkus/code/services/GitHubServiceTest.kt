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
        val repository = gitHubService.createRepository("cd32dac36836a790734e0cfc878d0258bca7a2ea", "repo-name")
        gitHubService.push("cd32dac36836a790734e0cfc878d0258bca7a2ea", repository, path)
    }
}