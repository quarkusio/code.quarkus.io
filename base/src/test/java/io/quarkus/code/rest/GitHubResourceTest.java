package io.quarkus.code.rest;

import io.quarkus.code.service.GitHubService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@QuarkusTest
class GitHubResourceTest {

    @Inject
    GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        QuarkusMock.installMockForType(new GitHubServiceMock(), GitHubService.class);
    }

    @Test
    void shouldCreateRepositoryWithCodeAndPushToGitHub() {
        RestAssured.given()
                .header("GitHub-Code", "gh-code")
                .header("GitHub-State", "someRandomState")
                .contentType(ContentType.JSON)
                .body("{\"artifactId\":\"test-artifact\"}")
                .when().post("/api/github/project")
                .then()
                .log().everything()
                .statusCode(200)
                .body("url", Matchers.equalTo("https://github.com/edewit/test-artifact"));
    }

    @Test
    void shouldReturnHttpConflictError409WhenRepositoryAlreadyExists() {
        RestAssured.given()
                .header("GitHub-Code", "gh-code")
                .header("GitHub-State", "someRandomState")
                .contentType(ContentType.JSON)
                .body("{\"artifactId\":\"existing-repo\"}")
                .when().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(409);
    }

    @Test
    void shouldFailIfGitHubCodeHeaderIsEmpty() {
        RestAssured.given()
                .header("GitHub-State", "someRandomState")
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    void shouldFailIfGitHubStateHeaderIsNull() {
        RestAssured.given()
                .header("GitHub-Code", "gh-code")
                .contentType(ContentType.JSON)
                .when().post("/api/github/project")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

}
