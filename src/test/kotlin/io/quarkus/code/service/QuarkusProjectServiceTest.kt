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
    lateinit var platformService: PlatformService

    @Test
    @DisplayName("When using default project, then, it should create the zip with all the files correctly with the requested content")
    fun testDefaultZip(info: TestInfo) {
        // When
        val creator = getProjectService()
        val proj = creator.create(platformService.recommendedPlatformInfo, ProjectDefinition())
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir = Paths.get(testDir.first.path, "code-with-quarkus")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>org.acme</groupId>"))
            .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
            .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
            .satisfies(checkContains("<quarkus.platform.group-id>${platformService.recommendedPlatformInfo.extensionCatalog.bom.groupId}</quarkus.platform.group-id>"))
            .satisfies(checkContains("<quarkus.platform.artifact-id>${platformService.recommendedPlatformInfo.extensionCatalog.bom.artifactId}</quarkus.platform.artifact-id>"))
            .satisfies(checkContains("<quarkus.platform.version>${platformService.recommendedPlatformInfo.extensionCatalog.bom.version}</quarkus.platform.version>")).satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"))

        assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
            .satisfies(checkContains("@Path(\"/hello\")"))
    }

    @Test
    @DisplayName("When using default project, then, it should create all the files correctly with the requested content")
    fun testDefault(info: TestInfo) {
        // When
        val creator = getProjectService()
        val projDir = creator.createTmp(platformService.recommendedPlatformInfo, ProjectDefinition())

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>org.acme</groupId>"))
            .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
            .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
            .satisfies(checkContains("<quarkus.platform.group-id>${platformService.recommendedPlatformInfo.extensionCatalog.bom.groupId}</quarkus.platform.group-id>"))
            .satisfies(checkContains("<quarkus.platform.artifact-id>${platformService.recommendedPlatformInfo.extensionCatalog.bom.artifactId}</quarkus.platform.artifact-id>"))
            .satisfies(checkContains("<quarkus.platform.version>${platformService.recommendedPlatformInfo.extensionCatalog.bom.version}</quarkus.platform.version>")).satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"))

        assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
            .satisfies(checkContains("@Path(\"/hello\")"))
    }

    @Test
    @DisplayName("When using a custom project, then, it should create all the files correctly with the requested content")
    fun testCustom(info: TestInfo) {
        // When
        val creator = getProjectService()
        val proj = creator.create(
            platformService.recommendedPlatformInfo,
            ProjectDefinition(
                groupId = "com.test",
                artifactId = "test-app",
                version = "2.0.0",
                className = "com.test.TestResource",
                path = "/test/it",
                extensions = setOf(
                    "io.quarkus:quarkus-resteasy",
                    "io.quarkus:quarkus-resteasy-jsonb",
                    "quarkus-neo4j",
                    "hibernate-validator"
                )
            )
        )
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir = Paths.get(testDir.first.path, "test-app")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>com.test</groupId>"))
            .satisfies(checkContains("<artifactId>test-app</artifactId>"))
            .satisfies(checkContains("<version>2.0.0</version>"))
            .satisfies(checkContains("<quarkus.platform.group-id>${platformService.recommendedPlatformInfo.extensionCatalog.bom.groupId}</quarkus.platform.group-id>"))
            .satisfies(checkContains("<quarkus.platform.artifact-id>${platformService.recommendedPlatformInfo.extensionCatalog.bom.artifactId}</quarkus.platform.artifact-id>"))
            .satisfies(checkContains("<quarkus.platform.version>${platformService.recommendedPlatformInfo.extensionCatalog.bom.version}</quarkus.platform.version>"))
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
            platformService.recommendedPlatformInfo,
            ProjectDefinition(
                groupId = "com.kot",
                artifactId = "test-kotlin-app",
                version = "2.0.0",
                buildTool = "GRADLE",
                className = "com.test.TestResource",
                extensions = setOf("resteasy", "kotlin")
            )
        )
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir = Paths.get(testDir.first.path, "test-kotlin-app")

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
    @DisplayName("Create a project with RESTEasy-Qute and YAML config")
    fun testQuteYaml(info: TestInfo) {
        // When
        val creator = getProjectService()

        val proj = creator.create(
            platformService.recommendedPlatformInfo,
            ProjectDefinition(
                groupId = "my.qute.yaml.app",
                artifactId = "test-qute-yaml-app",
                buildTool = "MAVEN",
                extensions = setOf("resteasy-qute", "config-yaml")
            )
        )
        val testDir = QuarkusProjectServiceTestUtils.extractProject(proj)
        val projDir = Paths.get(testDir.first.path, "test-qute-yaml-app")

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)
            .contains(
                "src/main/resources/templates/page.qute.html",
                "src/main/java/my/qute/yaml/app/GreetingConfig.java",
                "src/main/resources/application.yml",
                "src/main/java/my/qute/yaml/app/SomePage.java"
            )
    }

    @Test
    @Timeout(2)
    fun `Should create multiple project correctly`() {
        val executorService = Executors.newFixedThreadPool(4)

        val latch = CountDownLatch(20)
        val creator = getProjectService()
        val creates = (1..20).map { _ ->
            Callable {
                val result = creator.create(platformService.recommendedPlatformInfo, ProjectDefinition())
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
        return QuarkusProjectService()
    }
}
