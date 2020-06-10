package io.quarkus.code.service

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.service.QuarkusProjectServiceTestUtils.prefixFileList
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import javax.inject.Inject

@QuarkusTest
internal class QuarkusProjectServiceTest {

    companion object {
        val EXPECTED_CONTENT = arrayOf(
            "",
            "pom.xml",
            "src/",
            "src/main/",
            "src/main/java/",
            "src/main/java/org/",
            "src/main/java/org/acme/",
            "src/main/java/org/acme/ExampleResource.java",
            "src/test/",
            "src/test/java/",
            "src/test/java/org/",
            "src/test/java/org/acme/",
            "src/test/java/org/acme/ExampleResourceTest.java",
            "src/test/java/org/acme/NativeExampleResourceIT.java",
            "src/main/resources/",
            "src/main/resources/META-INF/",
            "src/main/resources/META-INF/resources/",
            "src/main/resources/META-INF/resources/index.html",
            "src/main/docker/",
            "src/main/docker/Dockerfile.native",
            "src/main/docker/Dockerfile.jvm",
            ".dockerignore",
            "src/main/resources/application.properties",
            "README.md",
            ".gitignore",
            ".mvn/",
            ".mvn/wrapper/",
            ".mvn/wrapper/maven-wrapper.jar",
            ".mvn/wrapper/maven-wrapper.properties",
            ".mvn/wrapper/MavenWrapperDownloader.java",
            "mvnw.cmd",
            "mvnw"
        )

        val EXPECTED_CONTENT_CUSTOM = arrayOf(
            "",
            "pom.xml",
            "src/",
            "src/main/",
            "src/main/java/",
            "src/main/java/com/",
            "src/main/java/com/test/",
            "src/main/java/com/test/TestResource.java",
            "src/test/",
            "src/test/java/",
            "src/test/java/com/",
            "src/test/java/com/test/",
            "src/test/java/com/test/TestResourceTest.java",
            "src/test/java/com/test/NativeTestResourceIT.java",
            "src/main/resources/",
            "src/main/resources/META-INF/",
            "src/main/resources/META-INF/resources/",
            "src/main/resources/META-INF/resources/index.html",
            "src/main/docker/",
            "src/main/docker/Dockerfile.native",
            "src/main/docker/Dockerfile.jvm",
            ".dockerignore",
            "src/main/resources/application.properties",
            "README.md",
            ".gitignore",
            ".mvn/",
            ".mvn/wrapper/",
            ".mvn/wrapper/maven-wrapper.jar",
            ".mvn/wrapper/maven-wrapper.properties",
            ".mvn/wrapper/MavenWrapperDownloader.java",
            "mvnw.cmd",
            "mvnw"
        )

        val EXPECTED_CONTENT_GRADLE_KOTLIN = arrayOf(
            "",
            "build.gradle",
            "settings.gradle",
            "gradle.properties",
            "src/",
            "src/main/",
            "src/main/kotlin/",
            "src/main/kotlin/com/",
            "src/main/kotlin/com/test/",
            "src/main/kotlin/com/test/TestResource.kt",
            "src/test/",
            "src/test/kotlin/",
            "src/test/kotlin/com/",
            "src/test/kotlin/com/test/",
            "src/test/kotlin/com/test/TestResourceTest.kt",
            "src/native-test/",
            "src/native-test/kotlin/",
            "src/native-test/kotlin/com/",
            "src/native-test/kotlin/com/test/",
            "src/native-test/kotlin/com/test/NativeTestResourceIT.kt",
            "src/main/resources/",
            "src/main/resources/META-INF/",
            "src/main/resources/META-INF/resources/",
            "src/main/resources/META-INF/resources/index.html",
            "src/main/docker/",
            "src/main/docker/Dockerfile.native",
            "src/main/docker/Dockerfile.jvm",
            ".dockerignore",
            "src/main/resources/application.properties",
            "README.md",
            ".gitignore",
            "gradle/",
            "gradle/wrapper/",
            "gradle/wrapper/gradle-wrapper.jar",
            "gradle/wrapper/gradle-wrapper.properties",
            "gradlew.bat",
            "gradlew"
        )

        val EXPECTED_CONTENT_GRADLE_SCALA = arrayOf(
            "",
            "build.gradle",
            "settings.gradle",
            "gradle.properties",
            "src/",
            "src/main/",
            "src/main/scala/",
            "src/main/scala/com/",
            "src/main/scala/com/test/",
            "src/main/scala/com/test/TestResource.scala",
            "src/test/",
            "src/test/scala/",
            "src/test/scala/com/",
            "src/test/scala/com/test/",
            "src/test/scala/com/test/TestResourceTest.scala",
            "src/native-test/",
            "src/native-test/scala/",
            "src/native-test/scala/com/",
            "src/native-test/scala/com/test/",
            "src/native-test/scala/com/test/NativeTestResourceIT.scala",
            "src/main/resources/",
            "src/main/resources/META-INF/",
            "src/main/resources/META-INF/resources/",
            "src/main/resources/META-INF/resources/index.html",
            "src/main/docker/",
            "src/main/docker/Dockerfile.native",
            "src/main/docker/Dockerfile.jvm",
            ".dockerignore",
            "src/main/resources/application.properties",
            "README.md",
            ".gitignore",
            "gradle/",
            "gradle/wrapper/",
            "gradle/wrapper/gradle-wrapper.jar",
            "gradle/wrapper/gradle-wrapper.properties",
            "gradlew.bat",
            "gradlew"
        )
    }

    @Inject
    lateinit var codeQuarkusConfig: CodeQuarkusConfig

    @Inject
    lateinit var quarkusExtensionCatalog: QuarkusExtensionCatalogService

    @Test
    fun `When using default project, then, it should create the zip with all the files correctly with the requested content`() {
        // When
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val proj = creator.create(ProjectDefinition())
        val (testDir, zipList) = QuarkusProjectServiceTestUtils.extractProject(proj)
        val fileList = QuarkusProjectServiceTestUtils.readFiles(testDir)
        val pomText = Paths.get(testDir.path, "code-with-quarkus/pom.xml")
            .toFile().readText(Charsets.UTF_8)
        val resourceText = Paths.get(testDir.path, "code-with-quarkus/src/main/java/org/acme/ExampleResource.java")
            .toFile().readText(Charsets.UTF_8)
        // Then
        assertThat(zipList, containsInAnyOrder(*prefixFileList(EXPECTED_CONTENT, "code-with-quarkus/")))

        assertThat(fileList.size, equalTo(34))

        assertThat(pomText, containsString("<groupId>org.acme</groupId>"))
        assertThat(pomText, containsString("<artifactId>code-with-quarkus</artifactId>"))
        assertThat(pomText, containsString("<version>1.0.0-SNAPSHOT</version>"))
        assertThat(pomText, containsString("<quarkus-plugin.version>${codeQuarkusConfig.quarkusVersion}</quarkus-plugin.version>"))

        assertThat(resourceText, containsString("@Path(\"/hello\")"))
    }

    @Test
    fun `When using default project, then, it should create all the files correctly with the requested content`() {
        // When
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val proj = creator.createTmp(ProjectDefinition())
        val fileList = QuarkusProjectServiceTestUtils.readFiles(proj.toFile())
        val pomText = proj.resolve("pom.xml")
                .toFile().readText(Charsets.UTF_8)
        val resourceText = proj.resolve("src/main/java/org/acme/ExampleResource.java")
                .toFile().readText(Charsets.UTF_8)
        // Then
        assertThat(fileList, containsInAnyOrder(*EXPECTED_CONTENT))

        assertThat(fileList.size, equalTo(32))

        assertThat(pomText, containsString("<groupId>org.acme</groupId>"))
        assertThat(pomText, containsString("<artifactId>code-with-quarkus</artifactId>"))
        assertThat(pomText, containsString("<version>1.0.0-SNAPSHOT</version>"))
        assertThat(pomText, containsString("<quarkus-plugin.version>${codeQuarkusConfig.quarkusVersion}</quarkus-plugin.version>"))

        assertThat(resourceText, containsString("@Path(\"/hello\")"))
    }

    @Test
    fun `When using a custom project, then, it should create all the files correctly with the requested content`() {
        // When
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val proj = creator.create(
            ProjectDefinition(
                groupId = "com.test",
                artifactId = "test-app",
                version = "2.0.0",
                className = "com.test.TestResource",
                path = "/test/it",
                extensions = setOf("io.quarkus:quarkus-resteasy-jsonb"),
                shortExtensions = "YjV.pDS"
            )
        )
        val (testDir, zipList) = QuarkusProjectServiceTestUtils.extractProject(proj)
        val fileList = QuarkusProjectServiceTestUtils.readFiles(testDir)
        val pomText = Paths.get(testDir.path, "test-app/pom.xml")
            .toFile().readText(Charsets.UTF_8)
        val resourceText = Paths.get(testDir.path, "test-app/src/main/java/com/test/TestResource.java")
            .toFile().readText(Charsets.UTF_8)

        // Then
        assertThat(zipList, containsInAnyOrder(*prefixFileList(EXPECTED_CONTENT_CUSTOM, "test-app/")))
        assertThat(fileList.size, equalTo(34))

        assertThat(pomText, containsString("<groupId>com.test</groupId>"))
        assertThat(pomText, containsString("<artifactId>test-app</artifactId>"))
        assertThat(pomText, containsString("<version>2.0.0</version>"))
        assertThat(pomText, containsString("<quarkus-plugin.version>${codeQuarkusConfig.quarkusVersion}</quarkus-plugin.version>"))
        assertThat(pomText, containsString("<groupId>io.quarkus</groupId>"))
        assertThat(pomText, containsString("<artifactId>quarkus-resteasy-jsonb</artifactId>"))
        assertThat(pomText, containsString("<artifactId>quarkus-hibernate-validator</artifactId>"))
        assertThat(pomText, containsString("<artifactId>quarkus-neo4j</artifactId>"))

        assertThat(resourceText, containsString("@Path(\"/test/it\")"))
    }

    @Test
    fun `Create a Gradle project using kotlin source`() {
        // When
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val proj = creator.create(
            ProjectDefinition(
                groupId = "com.test",
                artifactId = "test-app",
                version = "2.0.0",
                buildTool = "GRADLE",
                className = "com.test.TestResource",
                shortExtensions = "OxX"
            )
        )
        val (testDir, zipList) = QuarkusProjectServiceTestUtils.extractProject(proj)
        val fileList = QuarkusProjectServiceTestUtils.readFiles(testDir)
        val buildGradleText = Paths.get(testDir.path, "test-app/build.gradle")
            .toFile().readText(Charsets.UTF_8)
        val resourceText = Paths.get(testDir.path, "test-app/src/main/kotlin/com/test/TestResource.kt")
            .toFile().readText(Charsets.UTF_8)

        // Then
        assertThat(zipList, containsInAnyOrder(*prefixFileList(EXPECTED_CONTENT_GRADLE_KOTLIN, "test-app/")))

        assertThat(fileList.size, equalTo(39))

        assertThat(buildGradleText, containsString("id 'org.jetbrains.kotlin.jvm' version "))
        assertThat(buildGradleText, containsString("implementation 'io.quarkus:quarkus-kotlin'"))
        assertThat(buildGradleText, containsString("implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk"))
        assertThat(buildGradleText, containsString("group 'com.test'"))
        assertThat(buildGradleText, containsString("version '2.0.0'"))
        // Ensure dependency block is not duplicated (issue #5251)
        assertThat(buildGradleText.indexOf("implementation enforcedPlatform"), equalTo(buildGradleText.lastIndexOf("implementation enforcedPlatform")));

        assertThat(resourceText, containsString("fun hello() = \"hello\""))
    }

    @Test
    fun `Create a Gradle project using scala source`() {
        // When
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val proj = creator.create(
            ProjectDefinition(
                groupId = "com.test",
                artifactId = "test-app",
                version = "2.0.0",
                buildTool = "GRADLE",
                className = "com.test.TestResource",
                shortExtensions = "3e"
            )
        )
        val (testDir, zipList) = QuarkusProjectServiceTestUtils.extractProject(proj)
        val fileList = QuarkusProjectServiceTestUtils.readFiles(testDir)
        val buildGradleText = Paths.get(testDir.path, "test-app/build.gradle")
            .toFile().readText(Charsets.UTF_8)
        val resourceText = Paths.get(testDir.path, "test-app/src/main/scala/com/test/TestResource.scala")
            .toFile().readText(Charsets.UTF_8)

        // Then
        assertThat(zipList, containsInAnyOrder(*prefixFileList(EXPECTED_CONTENT_GRADLE_SCALA, "test-app/")))

        assertThat(fileList.size, equalTo(39))

        assertThat(buildGradleText, containsString("id 'scala'"))
        assertThat(buildGradleText, containsString("implementation 'io.quarkus:quarkus-scala'"))
        assertThat(buildGradleText, containsString("group 'com.test'"))
        assertThat(buildGradleText, containsString("version '2.0.0'"))
        // Ensure dependency block is not duplicated (issue #5251)
        assertThat(buildGradleText.indexOf("implementation enforcedPlatform"), equalTo(buildGradleText.lastIndexOf("implementation enforcedPlatform")));

        assertThat(resourceText, containsString("def hello() = \"hello\""))
    }

    @Test
    @Timeout(2)
    fun `Should create multiple project correctly`() {
        val executorService = Executors.newFixedThreadPool(4)

        val latch = CountDownLatch(20)
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val creates = (1..20).map { _ ->
            Callable {
                val result = creator.create(ProjectDefinition())
                latch.countDown()
                result
            }
        }
        executorService.invokeAll(creates)
        println("await")
        latch.await()
        println("done")
    }

}
