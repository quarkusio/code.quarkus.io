package io.quarkus.code

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class GitHubResourceTest {

    @Inject
    lateinit var gitHubService: GitHubServiceMock

    @Test
    @DisplayName("Should create a repository with code and push to github")
    fun testPushToGithub() {
        given()
                .header("token", "123")
                .queryParam("a", "code-with-quarkus").`when`().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("url", Matchers.equalTo("https://github.com/edewit/code-with-quarkus"))
    }

    @Test
    @DisplayName("Fetch the token from github")
    fun testAuth() {
        given()
                .queryParam("code", "code-with-quarkus")
                .queryParam("state", "someRandomState").`when`().get("/api/github/token")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .log().body()
                .body("accessToken", Matchers.equalTo("AccessToken"))
    }
}
