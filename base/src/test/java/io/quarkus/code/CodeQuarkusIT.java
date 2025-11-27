package io.quarkus.code;

import io.quarkus.code.misc.QuarkusProjectTestUtils;
import io.quarkus.devtools.testing.WrapperRunner;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.apache.commons.compress.archivers.ArchiveException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

@QuarkusTest
public class CodeQuarkusIT {

    @ParameterizedTest
    @ValueSource(strings = { "java" })
    @DisplayName("Should generate a maven project and run it in different language")
    void testMaven(String language) throws IOException, ArchiveException {
        String languageExt = language.equals("java") ? "" : "io.quarkus:quarkus-" + language;
        String appName = "test-app-maven-" + language;
        byte[] result = RestAssured.given()
                .when().get("/api/download?a=" + appName + "&e=neo4j&e=rest&e=" + languageExt + "&j=21")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + appName + ".zip\"")
                .extract().asByteArray();
        MatcherAssert.assertThat(result, CoreMatchers.notNullValue());
        var project = QuarkusProjectTestUtils.extractProject(result).getKey();
        var appDir = project.toPath().resolve(appName);
        int run = WrapperRunner.run(appDir, WrapperRunner.Wrapper.MAVEN);
        MatcherAssert.assertThat(run, CoreMatchers.is(0));
    }

    @ParameterizedTest
    @ValueSource(strings = { "java" })
    @DisplayName("Should generate a gradle project and run it in different language")
    void testGradle(String language) throws IOException, ArchiveException {
        String languageExt = language.equals("java") ? "" : "io.quarkus:quarkus-" + language;
        String appName = "test-app-gradle-" + language;
        byte[] result = RestAssured.given()
                .when()
                .get("/api/download?b=GRADLE&a=" + appName + "&v=1.0.0&e=neo4j&e=rest&e=" + languageExt + "&j=21")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + appName + ".zip\"")
                .extract().asByteArray();
        MatcherAssert.assertThat(result, CoreMatchers.notNullValue());
        var project = QuarkusProjectTestUtils.extractProject(result).getKey();
        int run = WrapperRunner.run(project.toPath().resolve(appName), WrapperRunner.Wrapper.GRADLE);
        MatcherAssert.assertThat(run, CoreMatchers.is(0));
    }

}
