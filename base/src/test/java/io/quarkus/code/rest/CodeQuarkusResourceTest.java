package io.quarkus.code.rest;

import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.code.service.PlatformService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

import java.security.cert.Extension;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class CodeQuarkusResourceTest {

    @Inject
    QuarkusProjectServiceMock projectService;

    @Inject
    PlatformService platformService;

    @Test
    void shouldFailWhenTooManyExtensions() {
        given()
                .contentType(ContentType.JSON)
                .body(ProjectDefinition.builder()
                        .extensions(platformService.recommendedPlatformInfo().extensionsById().keySet()).build())
                .when().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    void downloadFlowShouldWorkWithEmptyProject() {
        var path = given()
                .contentType(ContentType.JSON)
                .when().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path", equalTo("/d"))
                .extract().<String>path("path");
        given()
                .when().get(path)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.of()));
    }

    @Test
    void downloadAsPostShouldWorkWithEmptyProject() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/api/download")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.of()));
    }

    @Test
    void downloadAsPostShouldWorkWithNoExamplesLegacy() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"noCode\":true}")
                .when().post("/api/download")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.builder().noCode(true).build()));
    }

    @Test
    void downloadAsPostShouldWorkWithAllExtensions() {
        var projectDefinition =
                ProjectDefinition.builder().noCode(true).extensions(platformService.recommendedCodeQuarkusExtensions()
                        .stream()
                        .filter(extension -> !extension.category().equals("Alternative languages"))
                        .map(CodeQuarkusExtension::id)
                        .collect(Collectors.toSet())
                ).build();
        given()
                .contentType(ContentType.JSON)
                .body("{\"noCode\":true,\"extensions\":[\""
                        + String.join("\",\"", projectDefinition.extensions())
                        + "\"]}")
                .when().post("/api/download")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(projectDefinition));
    }

    @Test
    void downloadFlowShouldWorkWithGav() {
        var path = given()
                .contentType(ContentType.JSON)
                .body("{\"groupId\":\"io.andy\",\"artifactId\":\"my-app\",\"version\":\"1.0.0\"}")
                .when().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path", equalTo("/d?g=io.andy&a=my-app&v=1.0.0"))
                .extract().<String>path("path");
        given()
                .when().get(path)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"my-app.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.builder()
                .groupId("io.andy")
                .artifactId("my-app")
                .version("1.0.0")
                .build()));
    }

    @Test
    void downloadFlowShouldWorkWithAllOptions() {
        var path = given()
                .contentType(ContentType.JSON)
                .body("{\"groupId\":\"io.awesome\",\"artifactId\":\"my-awesome-app\",\"version\":\"2.0.0\",\"noCode\":true,\"extensions\":[\"io.quarkus:quarkus-resteasy\",\"io.quarkus:quarkus-resteasy-jackson\"]}")
                .when().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path",
                        equalTo("/d?g=io.awesome&a=my-awesome-app&v=2.0.0&nc=true&e=io.quarkus%3Aquarkus-resteasy&e=io.quarkus%3Aquarkus-resteasy-jackson"))
                .extract().<String>path("path");
        given()
                .when().urlEncodingEnabled(false).get(path)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"my-awesome-app.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.builder()
                .groupId("io.awesome")
                .artifactId("my-awesome-app")
                .version("2.0.0")
                .noCode(true)
                .extensions(Set.of("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-resteasy-jackson"))
                .build()));
    }

    @Test
    void downloadAsPostShouldWorkWithAllOptions() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"groupId\":\"io.awesome\",\"artifactId\":\"my-awesome-app\",\"version\":\"2.0.0\",\"javaVersion\":11,\"noCode\":true,\"extensions\":[\"io.quarkus:quarkus-resteasy\",\"io.quarkus:quarkus-resteasy-jackson\"]}")
                .when().post("/api/download")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"my-awesome-app.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.builder()
                .groupId("io.awesome")
                .artifactId("my-awesome-app")
                .version("2.0.0")
                .javaVersion(11)
                .noCode(true)
                .extensions(Set.of("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-resteasy-jackson"))
                .build()));
    }

    @Test
    void shouldReturnProjectWithDefaultConfigurationWhenNoParameters() {
        given()
                .when().get("/api/download")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"");
        assertThat(projectService.getCreatedProject(), equalTo(ProjectDefinition.of()));
    }

    @Test
    void shouldFailWhenArtifactIdIsEmpty() {
        given()
                .when()
                .get("/api/download?g=org.acme&a=&pv=1.0.0&c=org.acme.TotoResource&e=resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using invalid groupId")
    void testWithInvalidGroupId() {
        given()
                .when()
                .get("/api/download?g=org.acme.&e=resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using invalid artifactId")
    void testWithInvalidArtifactId() {
        given()
                .when()
                .get("/api/download?a=Art.&e=resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using invalid path")
    void testWithInvalidPath() {
        given()
                .when()
                .get("/api/download?p=invalid&e=resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using invalid className")
    void testWithInvalidClassName() {
        given()
                .when()
                .get("/api/download?c=com.1e&e=resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using invalid extensionId")
    void testWithInvalidExtensionId() {
        given()
                .when()
                .get("/api/download?e=inv")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using invalid javaVersion")
    void testWithInvalidJavaVersion() {
        given()
                .when()
                .get("/api/download?j=550")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when using javaVersion as text")
    void testWithInvalidJavaVersionString() {
        given()
                .when()
                .get("/api/download?j=text")
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should return a project with specified configuration when a few parameters are specified")
    void testWithAFewParams() {
        given()
                .when()
                .get("/api/download?a=test-app-with-a-few-arg&v=1.0.0&e=resteasy&e=resteasy-jackson")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"");
        assertThat(
                projectService.getCreatedProject(), equalTo(
                        ProjectDefinition.builder().artifactId("test-app-with-a-few-arg").version("1.0.0")
                                .extensions(Set.of("resteasy", "resteasy-jackson")).build()));

    }

    @Test
    @DisplayName("Should return a project with specified configuration when extensions is empty")
    void testWithEmptyExtensions() {
        given()
                .when()
                .get("/api/download?g=org.acme&a=test-empty-ext&v=1.0.1&b=MAVEN&c=org.test.ExampleResource&e=")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-empty-ext.zip\"");
        assertThat(
                projectService.getCreatedProject(), equalTo(
                        ProjectDefinition.builder().artifactId("test-empty-ext").version("1.0.1")
                                .className("org.test.ExampleResource")
                                .extensions(Set.of(""))
                                .build()));
    }

    @Test
    @DisplayName("Should return a project with the url rewrite")
    void testWithUrlRewrite() {
        given()
                .when()
                .get("/d?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=org.toto.TotoResource&e=logging-json&e=amazon-lambda-http&e=elytron-security-oauth2")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"");
        assertThat(
                projectService.getCreatedProject(), equalTo(
                        ProjectDefinition.builder().groupId("com.toto").artifactId("test-app").version("1.0.0")
                                .className("org.toto.TotoResource").path("/toto/titi")
                                .extensions(Set.of("logging-json", "amazon-lambda-http", "elytron-security-oauth2"))
                                .build()));
    }

    @Test
    @DisplayName("Should return a project with specified configuration when all parameters are specified")
    void testWithAllParams() {
        given()
                .when()
                .get("/api/download?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=org.toto.TotoResource&e=logging-json&e=amazon-lambda-http&e=elytron-security-oauth2")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"");
        assertThat(
                projectService.getCreatedProject(), equalTo(
                        ProjectDefinition.builder().groupId("com.toto").artifactId("test-app").version("1.0.0")
                                .className("org.toto.TotoResource").path("/toto/titi")
                                .extensions(Set.of("logging-json", "amazon-lambda-http", "elytron-security-oauth2"))
                                .build()));
    }

    @Test
    @DisplayName("Should return a project with specified with old extension syntax")
    void testWithOldExtensionSyntaxParams() {
        given()
                .when()
                .get("/api/download?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=com.toto.TotoResource&e=io.quarkus:quarkus-resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"");
        assertThat(
                projectService.getCreatedProject(), equalTo(
                        ProjectDefinition.builder().groupId("com.toto").artifactId("test-app").version("1.0.0")
                                .className("com.toto.TotoResource").path("/toto/titi")
                                .extensions(Set.of("io.quarkus:quarkus-resteasy")).build()));
    }

    @Test
    @DisplayName("Should return the default configuration")
    void testConfig() {
        given()
                .when().get("/api/config")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("environment", equalTo("dev"))
                .body("gitCommitId", notNullValue())
                .body("gaTrackingId", nullValue())
                .body("sentryDSN", nullValue())
                .body("quarkusPlatformVersion", notNullValue())
                .body("quarkusDevtoolsVersion", notNullValue());
    }

    @Test
    @DisplayName("Should return the extension list")
    void testExtensions() {
        given()
                .when().get("/api/extensions")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("$.size()", greaterThan(50));
    }

    @Test
    @DisplayName("Should return the requested extension")
    void testExtensionById() {
        given()
                .when().get("/api/extensions?id=io.quarkus:quarkus-resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("$.size()", equalTo(1))
                .body("[0].id", equalTo("io.quarkus:quarkus-resteasy"));
    }

    @Test
    @DisplayName("Should generate a gradle project")
    void testGradle() {
        given()
                .when()
                .get("/api/download?b=GRADLE&a=test-app-with-a-few-arg&v=1.0.0&e=neo4j&e=amazon-lambda-http")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"");
        assertThat(
                projectService.getCreatedProject(), equalTo(
                        ProjectDefinition.builder().artifactId("test-app-with-a-few-arg").version("1.0.0").buildTool("GRADLE")
                                .extensions(Set.of("neo4j", "amazon-lambda-http")).build()
                )
        );
    }
}
