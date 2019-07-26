package io.launcher.quarkus

import com.google.common.io.Files
import io.launcher.quarkus.model.QuarkusProject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipFile

internal class QuarkusProjectCreatorTest {

    @Test
    @DisplayName("Should create a project correctly")
    fun testCreateProject() {
        val creator = QuarkusProjectCreator()
        val proj = creator.create(QuarkusProject())
        val testDir = Files.createTempDir()
        val zipFile = File(testDir, "project.zip")
        zipFile.outputStream().use { output ->
            output.write(proj)
        }
        unzip(testDir, zipFile)
        val fileList = testDir.walkTopDown()
            .map { file -> file.relativeTo(testDir).toString() }
            .toList()
        assertThat(
            fileList, containsInAnyOrder(
                "",
                "project.zip",
                "quarkus-app",
                "quarkus-app/mvnw.cmd",
                "quarkus-app/pom.xml",
                "quarkus-app/.dockerignore",
                "quarkus-app/.gitignore",
                "quarkus-app/.mvn",
                "quarkus-app/.mvn/wrapper",
                "quarkus-app/.mvn/wrapper/maven-wrapper.properties",
                "quarkus-app/.mvn/wrapper/maven-wrapper.jar",
                "quarkus-app/.mvn/wrapper/MavenWrapperDownloader.java",
                "quarkus-app/mvnw",
                "quarkus-app/src",
                "quarkus-app/src/test",
                "quarkus-app/src/test/java",
                "quarkus-app/src/test/java/org",
                "quarkus-app/src/test/java/org/example",
                "quarkus-app/src/test/java/org/example/QuarkusAppTest.java",
                "quarkus-app/src/test/java/org/example/NativeQuarkusAppIT.java",
                "quarkus-app/src/main",
                "quarkus-app/src/main/docker",
                "quarkus-app/src/main/docker/Dockerfile.native",
                "quarkus-app/src/main/docker/Dockerfile.jvm",
                "quarkus-app/src/main/resources",
                "quarkus-app/src/main/resources/META-INF",
                "quarkus-app/src/main/resources/META-INF/resources",
                "quarkus-app/src/main/resources/META-INF/resources/index.html",
                "quarkus-app/src/main/resources/application.properties",
                "quarkus-app/src/main/java",
                "quarkus-app/src/main/java/org",
                "quarkus-app/src/main/java/org/example",
                "quarkus-app/src/main/java/org/example/QuarkusApp.java"
            )
        )
    }

    private fun unzip(outputDir: File, zipFile: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val file = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    zip.getInputStream(entry).use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

}