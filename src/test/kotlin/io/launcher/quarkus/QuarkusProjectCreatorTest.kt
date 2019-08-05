package io.launcher.quarkus

import io.launcher.quarkus.model.QuarkusProject
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


internal class QuarkusProjectCreatorTest {

    companion object {
        val EXPECTED_ZIP_CONTENT = arrayOf(
            "quarkus-app/",
            "quarkus-app/pom.xml",
            "quarkus-app/src/",
            "quarkus-app/src/main/",
            "quarkus-app/src/main/java/",
            "quarkus-app/src/main/java/org/",
            "quarkus-app/src/main/java/org/example/",
            "quarkus-app/src/main/java/org/example/ExampleResource.java",
            "quarkus-app/src/test/",
            "quarkus-app/src/test/java/",
            "quarkus-app/src/test/java/org/",
            "quarkus-app/src/test/java/org/example/",
            "quarkus-app/src/test/java/org/example/ExampleResourceTest.java",
            "quarkus-app/src/test/java/org/example/NativeExampleResourceIT.java",
            "quarkus-app/src/main/resources/",
            "quarkus-app/src/main/resources/META-INF/",
            "quarkus-app/src/main/resources/META-INF/resources/",
            "quarkus-app/src/main/resources/META-INF/resources/index.html",
            "quarkus-app/src/main/docker/",
            "quarkus-app/src/main/docker/Dockerfile.native",
            "quarkus-app/src/main/docker/Dockerfile.jvm",
            "quarkus-app/.dockerignore",
            "quarkus-app/src/main/resources/application.properties",
            "quarkus-app/.gitignore",
            "quarkus-app/.mvn/",
            "quarkus-app/.mvn/wrapper/",
            "quarkus-app/.mvn/wrapper/maven-wrapper.jar",
            "quarkus-app/.mvn/wrapper/maven-wrapper.properties",
            "quarkus-app/.mvn/wrapper/MavenWrapperDownloader.java",
            "quarkus-app/mvnw.cmd",
            "quarkus-app/mvnw"
        )
    }

    @Test
    @DisplayName("Should create a project correctly")
    fun testCreateProject() {
        val creator = QuarkusProjectCreator()
        val proj = creator.create(QuarkusProject())
        val testDir = Files.createTempDirectory("test-zip").toFile()
        println(testDir)
        val zipFile = testDir.resolve("project.zip")
        zipFile.outputStream().use { output ->
            output.write(proj)
        }
        val zipList = unzip(testDir, zipFile)
        assertThat(zipList, contains(*EXPECTED_ZIP_CONTENT))
        val fileList = testDir.walkTopDown()
            .map { file -> file.relativeTo(testDir).toString() }
            .toList()
        assertThat(fileList.size, equalTo(33))
    }

    @Test
    @DisplayName("Should create multiple project correctly")
    @Timeout(1)
    fun testCreateMultipleProject() {
        val executorService = Executors.newFixedThreadPool(10)

        val latch = CountDownLatch(50)
        val creator = QuarkusProjectCreator()
        val creates = (1..50).map {i ->
            Callable {
                val result = creator.create(QuarkusProject())
                latch.countDown()
                result
            }
        }
        executorService.invokeAll(creates)
        println("await")
        latch.await()
        println("done")
    }

    private fun unzip(outputDir: File, zipFile: File): List<String> {
        zipFile.inputStream().use { zfis ->
            ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, zfis).use { zip ->
                var entry: ZipArchiveEntry?
                val list = arrayListOf<String>()
                do {
                    entry = (zip as ZipArchiveInputStream).nextZipEntry
                    if (entry == null)
                        break
                    list.add(entry.name)
                    val file = File(outputDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        if (!file.parentFile.exists()) {
                            file.parentFile.mkdirs()
                        }
                        file.outputStream().use { output ->
                            zip.copyTo(output)
                        }
                    }
                } while (true)
                return list
            }
        }

    }

}