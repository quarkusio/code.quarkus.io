package io.quarkus.generator.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@QuarkusTest
internal class LauncherQuarkusTest {

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
}