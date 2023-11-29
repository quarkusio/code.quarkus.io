package io.quarkus.code

import io.quarkus.code.service.QuarkusProjectServiceTestUtils
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import io.quarkus.devtools.testing.WrapperRunner

@QuarkusTest
class CodeQuarkusIT {

    @ParameterizedTest
    @ValueSource(strings = ["java"])
    @DisplayName("Should generate a maven project and run it in different language")
    fun testMaven(language: String) {
        val languageExt = if(language != "java") "io.quarkus:quarkus-$language" else ""
        val appName = "test-app-maven-$language"
        val result = given()
                .`when`().get("/api/download?a=$appName&e=neo4j&e=resteasy-reactive&e=$languageExt&j=21")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"$appName.zip\"")
                .extract().asByteArray()
        assertThat(result, notNullValue())
        val dir = QuarkusProjectServiceTestUtils.extractProject(result).first
        val appDir = dir.toPath().resolve(appName)
        val run = WrapperRunner.run(appDir, WrapperRunner.Wrapper.MAVEN)
        assertThat(run, `is`(0))
    }

    @ParameterizedTest
    @ValueSource(strings = ["java"])
    @DisplayName("Should generate a gradle project and run it in different language")
    fun testGradle(language: String) {
        val languageExt = if(language != "java") "io.quarkus:quarkus-$language" else ""
        val appName = "test-app-gradle-$language"
        val result = given()
                .`when`().get("/api/download?b=GRADLE&a=$appName&v=1.0.0&e=neo4j&e=resteasy-reactive&e=$languageExt&j=21")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"$appName.zip\"")
                .extract().asByteArray()
        assertThat(result, notNullValue())
        val dir = QuarkusProjectServiceTestUtils.extractProject(result).first
        val run = WrapperRunner.run(dir.toPath().resolve(appName), WrapperRunner.Wrapper.GRADLE)
        assertThat(run, `is`(0))
    }

}
