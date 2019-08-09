package io.launcher.quarkus

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.ws.rs.core.MediaType

@QuarkusTest
class LauncherQuarkusResourceTest {

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @Test
    @DisplayName("Should return a project with default configuration when there is no parameters")
    fun testNoParams() {
        given()
            .`when`().get("/api/quarkus/download")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"")
    }

    @Test
    @DisplayName("Should fail when a param is specified has empty")
    fun testWithEmptyParam() {
        given()
            .`when`()
            .get("/api/quarkus/download?g=org.acme&a=&pv=1.0.0&c=org.acme.TotoResource&e=io.quarkus:quarkus-resteasy")
            .then()
            .statusCode(400)
    }

    @Test
    @DisplayName("Should return a project with specified configuration when parameters are specified")
    fun testWithSpecifiedParams() {
        given()
            .`when`()
            .get("/api/quarkus/download?g=org.toto&a=test-app&pv=1.0.0&p=%2Ftoto&c=org.toto.TotoResource&e=io.quarkus:quarkus-resteasy&e=io.quarkus:quarkus-resteasy-jsonb")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")


        /* assertThat((projectCreator as QuarkusProjectCreatorMock).createdProjectRef.get(), equalTo(QuarkusProject(
            groupId = "com.toto",
            artifactId = "test-app",
            version = "1.0.0",
            className = "org.toto.TotoResource",
            path = "/toto",
            extensions = setOf("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-resteasy-jsonb")
        ))) */
    }

    @Test
    @DisplayName("Should return the default configuration")
    fun testConfig() {
        given()
            .`when`().get("/api/quarkus/config")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("environment", equalTo("dev"))
            .body("gaTrackingId", nullValue())
            .body("sentryDSN", nullValue())
    }

    @Test
    @DisplayName("Should return the extension list")
    fun testExtensions() {
        given()
            .`when`().get("/api/quarkus/extensions")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$.size()", `is`(53))
    }
}