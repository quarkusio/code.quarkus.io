package io.quarkus.code.rest

import io.quarkus.code.service.GitHubService
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import jakarta.inject.Inject

@QuarkusTest
class GitHubResourceTest {

    @Inject
    lateinit var gitHubService: GitHubService

    @BeforeEach
    fun setUp() {
        QuarkusMock.installMockForType(GitHubServiceMock(), GitHubService::class.java)
    }

    @Test
    fun `Should create a repository with code and push to github`() {
        given()
            .header("GitHub-Code", "gh-code")
            .header("GitHub-State", "someRandomState")
            .contentType(ContentType.JSON)
            .body("{\"artifactId\":\"test-artifact\"}")
            .`when`().post("/api/github/project")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("url", Matchers.equalTo("https://github.com/edewit/test-artifact"))
    }

    @Test
    fun `Should return a http conflict error 409 when the repository already exists`() {
        given()
            .header("GitHub-Code", "gh-code")
            .header("GitHub-State", "someRandomState")
            .contentType(ContentType.JSON)
            .body("{\"artifactId\":\"existing-repo\"}")
            .`when`().post("/api/github/project")
            .then()
            .log().ifValidationFails()
            .statusCode(409)
    }

    @Test
    fun `Should fail if GitHub-Code header is empty`() {
        given()
            .header("GitHub-State", "someRandomState")
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`().post("/api/github/project")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `Should fail if GitHub-State header is null`() {
        given()
            .header("GitHub-Code", "gh-code")
            .contentType(ContentType.JSON)
            .`when`().post("/api/github/project")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

}
