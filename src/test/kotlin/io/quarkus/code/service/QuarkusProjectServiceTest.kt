package io.quarkus.code.service

import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.devtools.testing.SnapshotTesting.*
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.Timeout
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import javax.inject.Inject

@QuarkusTest
internal class QuarkusProjectServiceTest {

    @Inject
    lateinit var codeQuarkusConfig: CodeQuarkusConfig

    @Inject
    lateinit var quarkusExtensionCatalog: QuarkusExtensionCatalogService

    @Test
    @DisplayName("When using default project, then, it should create the zip with all the files correctly with the requested content")
    fun testDefaultZip(info: TestInfo) {
        // When
        val creator = getProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val proj = creator.create(ProjectDefinition())
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir =  Paths.get(testDir.first.path, "code-with-quarkus")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>org.acme</groupId>"))
            .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
            .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
            .satisfies(checkContains("<quarkus-plugin.version>${codeQuarkusConfig.quarkusVersion}</quarkus-plugin.version>"))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"))

        assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
            .satisfies(checkContains("@Path(\"/hello-resteasy\")"))
    }

    @Test
    @DisplayName("When using default project, then, it should create all the files correctly with the requested content")
    fun testDefault(info: TestInfo) {
        // When
        val creator = getProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        val projDir = creator.createTmp(ProjectDefinition())

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>org.acme</groupId>"))
            .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
            .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
            .satisfies(checkContains("<quarkus-plugin.version>${codeQuarkusConfig.quarkusVersion}</quarkus-plugin.version>"))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"))

        assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
            .satisfies(checkContains("@Path(\"/hello-resteasy\")"))
    }

    @Test
    @DisplayName("When using a custom project, then, it should create all the files correctly with the requested content")
    fun testCustom(info: TestInfo) {
        // When
        val creator = getProjectService()
        val proj = creator.create(
            ProjectDefinition(
                groupId = "com.test",
                artifactId = "test-app",
                version = "2.0.0",
                className = "com.test.TestResource",
                path = "/test/it",
                extensions = setOf("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-resteasy-jsonb"),
                shortExtensions = "YjV.pDS"
            )
        )
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir =  Paths.get(testDir.first.path, "test-app")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>com.test</groupId>"))
            .satisfies(checkContains("<artifactId>test-app</artifactId>"))
            .satisfies(checkContains("<version>2.0.0</version>"))
            .satisfies(checkContains("<quarkus-plugin.version>${codeQuarkusConfig.quarkusVersion}</quarkus-plugin.version>"))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy</artifactId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy-jsonb</artifactId>"))
            .satisfies(checkContains("<artifactId>quarkus-hibernate-validator</artifactId>"))
            .satisfies(checkContains("<artifactId>quarkus-neo4j</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"))

        assertThatMatchSnapshot(info, projDir, "src/main/java/com/test/TestResource.java")
            .satisfies(checkContains("@Path(\"/test/it\")"))
    }

    @Test
    @DisplayName("Create a Gradle project using kotlin source")
    fun testGradleKotlin(info: TestInfo) {
        // When
        val creator = getProjectService()

        val proj = creator.create(
            ProjectDefinition(
                groupId = "com.kot",
                artifactId = "test-kotlin-app",
                version = "2.0.0",
                buildTool = "GRADLE",
                className = "com.test.TestResource",
                extensions = setOf("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-kotlin")
            )
        )
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir =  Paths.get(testDir.first.path, "test-kotlin-app")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)
        assertThatMatchSnapshot(info, projDir, "settings.gradle")
            .satisfies(checkContains("rootProject.name='test-kotlin-app'"))
        assertThat(projDir.resolve("build.gradle"))
            .satisfies(checkContains("id 'org.jetbrains.kotlin.jvm' version "))
            .satisfies(checkContains("implementation 'io.quarkus:quarkus-resteasy'"))
            .satisfies(checkContains("implementation 'io.quarkus:quarkus-kotlin'"))
            .satisfies(checkContains("implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk"))
            .satisfies(checkContains("group 'com.kot'"))
            .satisfies(checkContains("version '2.0.0'"))

        assertThatMatchSnapshot(info, projDir, "src/main/kotlin/com/test/TestResource.kt")
            .satisfies(checkContains("fun hello() = \"Hello RESTEasy\""))
    }

    @Test
    @DisplayName("Create a Gradle project using scala source")
    fun testGradleScala(info: TestInfo) {
        // When
        val creator = getProjectService()

        val proj = creator.create(
            ProjectDefinition(
                groupId = "com.sc",
                artifactId = "test-scala-app",
                version = "3.0.0",
                buildTool = "GRADLE",
                className = "com.test.TestResource",
                extensions = setOf("io.quarkus:quarkus-resteasy", "io.quarkus:quarkus-scala")
            )
        )
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir =  Paths.get(testDir.first.path, "test-scala-app")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)
        assertThatMatchSnapshot(info, projDir, "settings.gradle")
            .satisfies(checkContains("rootProject.name='test-scala-app'"))
        assertThat(projDir.resolve("gradle.properties"))
            .satisfies(checkContains("quarkusPluginVersion=${codeQuarkusConfig.quarkusVersion}"))
        assertThat(projDir.resolve("build.gradle"))
            .satisfies(checkContains("id 'scala'"))
            .satisfies(checkContains("implementation 'io.quarkus:quarkus-scala'"))
            .satisfies(checkContains("implementation 'io.quarkus:quarkus-resteasy'"))
            .satisfies(checkContains("group 'com.sc'"))
            .satisfies(checkContains("version '3.0.0'"))

        assertThatMatchSnapshot(info, projDir, "src/main/scala/com/test/TestResource.scala")
            .satisfies(checkContains("def hello() = \"Hello RESTEasy\""))
    }

    @Test
    @Timeout(2)
    fun `Should create multiple project correctly`() {
        val executorService = Executors.newFixedThreadPool(4)

        val latch = CountDownLatch(20)
        val creator = getProjectService()
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

    private fun getProjectService(): QuarkusProjectService {
        val creator = QuarkusProjectService()
        creator.extensionCatalog = quarkusExtensionCatalog
        return creator
    }
}
