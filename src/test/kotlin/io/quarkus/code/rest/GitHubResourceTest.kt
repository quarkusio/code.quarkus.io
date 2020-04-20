package io.quarkus.code.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class GitHubResourceTest {

    @Inject
    lateinit var gitHubService: GitHubServiceMock

    @Test
    fun `Should create a repository with code and push to github`() {
        given()
                .header("GitHub-Code", "gh-code")
                .header("GitHub-State", "someRandomState")
                .queryParam("a", "code-with-quarkus").`when`().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("url", Matchers.equalTo("https://github.com/edewit/code-with-quarkus"))
    }

    @Test
    fun `Should return a http conflict error 409 when the repository already exists`() {
        given()
                .header("GitHub-Code", "gh-code")
                .header("GitHub-State", "someRandomState")
                .queryParam("a", "existing-repo").`when`().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(409)
    }

    @Test
    fun `Should fail if GitHub-Code header is empty`() {
        given()
                .header("GitHub-State", "someRandomState")
                .queryParam("a", "code-with-quarkus").`when`().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    fun `Should fail if GitHub-State header is null`() {
        given()
                .header("GitHub-Code", "gh-code")
                .queryParam("a", "code-with-quarkus").`when`().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

}
