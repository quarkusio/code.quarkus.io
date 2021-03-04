package io.quarkus.code.rest

import io.quarkus.code.model.ProjectDefinition
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.ws.rs.core.MediaType

@QuarkusTest
class CodeQuarkusResourceTest {

    @Inject
    lateinit var projectService: QuarkusProjectServiceMock

    @Test
    fun `Should fail when too many extensions`() {
        given()
            .contentType(ContentType.JSON)
            .body(ProjectDefinition(extensions = projectService.extensionCatalog.extensionsById.keys))
            .`when`().post("/api/project")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `Download flow should work with an empty project`() {
        val path = given()
                .contentType(ContentType.JSON)
                .`when`().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path", equalTo("/d"))
                .extract().path<String>("path")
        given()
                .`when`().get(path)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(ProjectDefinition()))
    }

    @Test
    fun `Download as POST should work with an empty project`() {
        val path = given()
            .contentType(ContentType.JSON)
            .`when`().post("/api/download")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(ProjectDefinition()))
    }

    @Test
    fun `Download flow should work with gav`() {
        val projectDefinition = ProjectDefinition("io.andy", "my-app", "1.0.0")
        val path = given()
                .contentType(ContentType.JSON)
                .body(projectDefinition)
                .`when`().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path", equalTo("/d?g=io.andy&a=my-app&v=1.0.0"))
                .extract().path<String>("path")
        given()
                .`when`().get(path)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"my-app.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(projectDefinition))
    }

    @Test
    fun `Download flow should work with all options`() {
        val projectDefinition = ProjectDefinition(
                groupId = "io.awesome",
                artifactId = "my-awesome-app",
                version = "2.0.0",
                noExamples = true,
                extensions = setOf("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-resteasy-jackson")
        )
        val path = given()
                .contentType(ContentType.JSON)
                .body(projectDefinition)
                .`when`().post("/api/project")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path", equalTo("/d?g=io.awesome&a=my-awesome-app&v=2.0.0&ne=true&e=io.quarkus%3Aquarkus-resteasy&e=io.quarkus%3Aquarkus-resteasy-jackson"))
                .extract().path<String>("path")
        given()
                .`when`().urlEncodingEnabled(false).get(path)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"my-awesome-app.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(projectDefinition))
    }

    @Test
    fun `Download as POST should work with all options`() {
        val projectDefinition = ProjectDefinition(
            groupId = "io.awesome",
            artifactId = "my-awesome-app",
            version = "2.0.0",
            noExamples = true,
            extensions = setOf("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-resteasy-jackson")
        )
        given()
            .contentType(ContentType.JSON)
            .body(projectDefinition)
            .`when`().post("/api/download")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"my-awesome-app.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(projectDefinition))
    }

    @Test
    fun `Should return a project with default configuration when there is no parameters`() {
        given()
                .`when`().get("/api/download")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(ProjectDefinition()))
    }

    @Test
    fun `Should fail when artifactId is empty`() {
        given()
                .`when`()
                .get("/api/download?g=org.acme&a=&pv=1.0.0&c=org.acme.TotoResource&s=98e")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should fail when using invalid groupId")
    fun testWithInvalidGroupId() {
        given()
                .`when`()
                .get("/api/download?g=org.acme.&s=98e")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should fail when using invalid artifactId")
    fun testWithInvalidArtifactId() {
        given()
                .`when`()
                .get("/api/download?a=Art.&s=98e")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should fail when using invalid path")
    fun testWithInvalidPath() {
        given()
                .`when`()
                .get("/api/download?p=invalid&s=98e")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should fail when using invalid className")
    fun testWithInvalidClassName() {
        given()
                .`when`()
                .get("/api/download?c=com.1e&s=98e")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should fail when using invalid shortId")
    fun testWithInvalidShortId() {
        given()
                .`when`()
                .get("/api/download?s=inv")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should fail when using invalid extensionId")
    fun testWithInvalidExtensionId() {
        given()
                .`when`()
                .get("/api/download?e=inv")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
    }

    @Test
    @DisplayName("Should return a project with specified configuration when a few parameters are specified")
    fun testWithAFewParams() {
        given()
                .`when`()
                .get("/api/download?a=test-app-with-a-few-arg&v=1.0.0&s=D9x.9Ie")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-app-with-a-few-arg",
                        version = "1.0.0",
                        shortExtensions = "D9x.9Ie"
                ))
        )

    }

    @Test
    @DisplayName("Should return a project with specified configuration when shortIds is empty")
    fun testWithEmptyShortIds() {
        given()
                .`when`()
                .get("/api/download?g=org.acme&a=test-empty-shortids&v=1.0.1&b=MAVEN&s=")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-empty-shortids.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-empty-shortids",
                        version = "1.0.1"
                ))
        )
    }

    @Test
    @DisplayName("Should return a project with specified configuration when extensions is empty")
    fun testWithEmptyExtensions() {
        given()
                .`when`()
                .get("/api/download?g=org.acme&a=test-empty-ext&v=1.0.1&b=MAVEN&c=org.test.ExampleResource&e=")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-empty-ext.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-empty-ext",
                        version = "1.0.1",
                        className = "org.test.ExampleResource",
                        extensions = setOf("")
                ))
        )
    }

    @Test
    @DisplayName("Should return a project with the url rewrite")
    fun testWithUrlRewrite() {
        given()
                .`when`()
                .get("/d?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=org.toto.TotoResource&e=logging-json&e=amazon-lambda-http&e=elytron-security-oauth2")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        groupId = "com.toto",
                        artifactId = "test-app",
                        version = "1.0.0",
                        className = "org.toto.TotoResource",
                        path = "/toto/titi",
                        extensions = setOf("logging-json", "amazon-lambda-http", "elytron-security-oauth2")
                )
        )
        )
    }

    @Test
    @DisplayName("Should return a project with specified configuration when all parameters are specified")
    fun testWithAllParams() {
        given()
                .`when`()
                .get("/api/download?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=org.toto.TotoResource&e=logging-json&e=amazon-lambda-http&e=elytron-security-oauth2")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        groupId = "com.toto",
                        artifactId = "test-app",
                        version = "1.0.0",
                        className = "org.toto.TotoResource",
                        path = "/toto/titi",
                        extensions = setOf("logging-json", "amazon-lambda-http", "elytron-security-oauth2")
                ))
        )
    }

    @Test
    @DisplayName("Should return a project with specified with old extension syntax")
    fun testWithOldExtensionSyntaxParams() {
        given()
                .`when`()
                .get("/api/download?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=com.toto.TotoResource&e=io.quarkus:quarkus-resteasy")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        groupId = "com.toto",
                        artifactId = "test-app",
                        version = "1.0.0",
                        className = "com.toto.TotoResource",
                        path = "/toto/titi",
                        extensions = setOf("io.quarkus:quarkus-resteasy")
                )
        )
        )
    }

    @Test
    @DisplayName("Should return the default configuration")
    fun testConfig() {
        given()
                .`when`().get("/api/config")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("environment", equalTo("dev"))
                .body("gitCommitId", notNullValue())
                .body("gaTrackingId", nullValue())
                .body("sentryDSN", nullValue())
                .body("quarkusVersion", notNullValue())
                .body("features", equalTo(listOf<String>()))
    }

    @Test
    @DisplayName("Should return the extension list")
    fun testExtensions() {
        given()
                .`when`().get("/api/extensions")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("$.size()", greaterThan(50))
    }

    @Test
    @DisplayName("Should generate a gradle project")
    fun testGradle() {
        given()
                .`when`()
                .get("/api/download?b=GRADLE&a=test-app-with-a-few-arg&v=1.0.0&e=neo4j&e=amazon-lambda-http")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-app-with-a-few-arg",
                        version = "1.0.0",
                        buildTool = "GRADLE",
                        extensions = setOf("neo4j", "amazon-lambda-http")
                )
        )
        )
    }
}
