package io.launcher.quarkus

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MediaType

@QuarkusTest
internal class LauncherQuarkusResourceTest {

    @Test
    @DisplayName("Should return a project with default configuration when there is no parameters")
    fun testNoParams() {
        given()
            .`when`().get("/api/quarkus/download")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"quarkus-app.zip\"")
    }

    @Test
    @DisplayName("Should return a project with specified configuration when parameters are specified")
    fun testWithSpecifiedParams() {
        given()
            .`when`()
            .get("/api/quarkus/download?g=org.acme&a=test-app&pv=1.0.0&c=org.acme.TotoResource&e=io.quarkus:quarkus-resteasy")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
    }

    @Test
    @DisplayName("Should return the default configuration")
    fun testConfig() {
        given()
            .`when`().get("/api/quarkus/config")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("environment", CoreMatchers.equalTo("dev"))
            .body("gaTrackingId", CoreMatchers.nullValue())
            .body("sentryDSN", CoreMatchers.nullValue())
    }
}