package io.quarkus.code.service

import io.quarkus.code.config.GitHubConfig
import io.quarkus.code.model.GitHubToken
import io.quarkus.code.service.GitHubServiceTest.CustomSimulationPreprocessor
import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.SimulationPreprocessor
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher
import io.specto.hoverfly.junit.core.model.Simulation
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflySimulate
import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.wildfly.common.Assert
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.util.*
import javax.net.ssl.SSLContext


/**
 * To record:
 * 1. Get a "code" using https://github.com/login/oauth/authorize?client_id=e1177a88a6d9eec4bd16&scope=public_repo&state=lMqTg6m6A-wbzQsCv2qW8jW-y0Y (it's in the query param of the redirection)
 * 2. Set the constant in the companion object CODE, LOGIN and AN_EXISTING_REPO
 * 4. Replace @HoverflySimulate by @HoverflyCapture
 * 5. Run all tests (should all pass)
 * 6. Replace @HoverflyCapture by @HoverflySimulate
 * 7. ----WARNING----- Replace your access_token in the recoding: io_quarkus_code_service_GitHubServiceTest.json
 */
@HoverflySimulate(config = HoverflyConfig(destination = ["github.com"], statefulCapture = true, simulationPreprocessor = CustomSimulationPreprocessor::class))
@ExtendWith(HoverflyExtension::class)
internal class GitHubServiceTest {

    companion object {
        const val CLIENT_ID = "e1177a88a6d9eec4bd16"
        const val CLIENT_SECRET = "8245b9a14dbfe8c931aa8080487117e3911b5917"
        const val STATE = "lMqTg6m6A-wbzQsCv2qW8jW-y0Y"

        // TO SET FOR CAPTURE (Read instruction at the top)
        const val CODE = "4bda4402b8a1c54771b6"
        const val LOGIN = "ia3andy"
        const val AN_EXISTING_REPO = "code.quarkus.io"
        const val REPO_TO_CREATE_NAME = "code.quarkus.io-testing-repo"

        private lateinit var gitHubService: GitHubService
        private lateinit var token: GitHubToken

        @BeforeAll
        @JvmStatic
        internal fun beforeAll(hoverfly: Hoverfly) {
            gitHubService = GitHubService()
            gitHubService.oauthClient = newRestClientWithSSLContext("https://github.com", hoverfly.sslConfigurer.sslContext, GitHubOAuthClient::class.java)
            gitHubService.ghClient = newRestClientWithSSLContext("https://api.github.com", hoverfly.sslConfigurer.sslContext, GitHubClient::class.java)
            gitHubService.config = GitHubConfigImpl(Optional.of(CLIENT_ID), Optional.of(CLIENT_SECRET))
            token = gitHubService.fetchAccessToken(CODE, STATE)
            assertThat(token, not(nullValue()))
            assertThat(token.accessToken, not(emptyOrNullString()))
        }

        fun <T> newRestClientWithSSLContext(baseUrl: String, sslContext: SSLContext, clazz: Class<T>): T {
            return RestClientBuilder.newBuilder()
                    .sslContext(sslContext)
                    .baseUrl(URL(baseUrl))
                    .build(clazz)
        }
    }

    @Test
    fun `Should return the user`() {
        assertThat(gitHubService.login(token.accessToken), `is`(LOGIN))
    }

    @Test
    fun `Should throw an exception when using an invalid code`() {
        assertThrows<IOException> {
            gitHubService.fetchAccessToken("invalidcode", STATE)
        }
    }

    @Test
    fun createAndPushRepository() {
        //given
        val path = Files.createTempDirectory("github-service-test")
        Files.copy(GitHubServiceTest::class.java.getResourceAsStream("/fakeextensions.json"), File(path.toString(), "test.json").toPath())

        //when
        val result = gitHubService.createRepository(LOGIN, token.accessToken, REPO_TO_CREATE_NAME)
        assertThat(result.url, `is`("https://github.com/$LOGIN/$REPO_TO_CREATE_NAME.git"))
        assertThat(result.ownerName, `is`(LOGIN))
        gitHubService.push(result.ownerName, token.accessToken, "main", result.url, path)
    }

    @Test
    fun `Should return true when the repository exists`() {
        val exists = gitHubService.repositoryExists(LOGIN, token.accessToken, AN_EXISTING_REPO)
        Assert.assertTrue(exists)
    }

    @Test
    fun `Should return false when the repository doesn't exist`() {
        val exists = gitHubService.repositoryExists(LOGIN, token.accessToken, "new-repo-name")
        Assert.assertFalse(exists)
    }

    data class GitHubConfigImpl(override val clientId: Optional<String>, override val clientSecret: Optional<String>) : GitHubConfig

    internal class CustomSimulationPreprocessor : SimulationPreprocessor {
        override fun accept(simulation: Simulation?) {
            // Change the git-receive-pack matcher to a wildcard because the body will vary
            val receivePackBodyMatcher = simulation!!.hoverflyData.pairs
                    .find { (it.request.path[0]?.value as String).contains("git-receive-pack") }
                    ?.request?.body?.get(0) as RequestFieldMatcher<Any>
            receivePackBodyMatcher.matcher = RequestFieldMatcher.MatcherType.GLOB
            receivePackBodyMatcher.value = "*"
        }
    }
}