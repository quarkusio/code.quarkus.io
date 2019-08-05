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
            "code-with-quarkus/",
            "code-with-quarkus/pom.xml",
            "code-with-quarkus/src/",
            "code-with-quarkus/src/main/",
            "code-with-quarkus/src/main/java/",
            "code-with-quarkus/src/main/java/org/",
            "code-with-quarkus/src/main/java/org/acme/",
            "code-with-quarkus/src/main/java/org/acme/ExampleResource.java",
            "code-with-quarkus/src/test/",
            "code-with-quarkus/src/test/java/",
            "code-with-quarkus/src/test/java/org/",
            "code-with-quarkus/src/test/java/org/acme/",
            "code-with-quarkus/src/test/java/org/acme/ExampleResourceTest.java",
            "code-with-quarkus/src/test/java/org/acme/NativeExampleResourceIT.java",
            "code-with-quarkus/src/main/resources/",
            "code-with-quarkus/src/main/resources/META-INF/",
            "code-with-quarkus/src/main/resources/META-INF/resources/",
            "code-with-quarkus/src/main/resources/META-INF/resources/index.html",
            "code-with-quarkus/src/main/docker/",
            "code-with-quarkus/src/main/docker/Dockerfile.native",
            "code-with-quarkus/src/main/docker/Dockerfile.jvm",
            "code-with-quarkus/.dockerignore",
            "code-with-quarkus/src/main/resources/application.properties",
            "code-with-quarkus/.gitignore",
            "code-with-quarkus/.mvn/",
            "code-with-quarkus/.mvn/wrapper/",
            "code-with-quarkus/.mvn/wrapper/maven-wrapper.jar",
            "code-with-quarkus/.mvn/wrapper/maven-wrapper.properties",
            "code-with-quarkus/.mvn/wrapper/MavenWrapperDownloader.java",
            "code-with-quarkus/mvnw.cmd",
            "code-with-quarkus/mvnw"
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