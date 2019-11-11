package io.quarkus.code

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@QuarkusTest
class GitHubResourceTest {

    @Test
    @DisplayName("Should create a repository with code and push to github")
    fun testPushToGithub() {
        given()
                .header("token", "123")
                .queryParam("a", "code-with-quarkus").`when`().get("/api/github/push")
                .then()
                .statusCode(200)
                .body("repository", equalTo("https://github.com/edewit/code-with-quarkus"))
    }

    @Test
    @DisplayName("Fetch the token from github")
    fun testAuth() {
        given()
                .queryParam("code", "code-with-quarkus")
                .queryParam("state", "someRandomState").`when`().get("/api/github/auth")
                .then()
                .statusCode(200)
                .body("token", equalTo("AccessToken"))
    }
}