package io.quarkus.code.service;

import io.quarkus.code.config.CodeQuarkusConfig;
import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.quarkus.devtools.testing.SnapshotTesting.assertThatDirectoryTreeMatchSnapshots;
import static io.quarkus.devtools.testing.SnapshotTesting.assertThatMatchSnapshot;
import static io.quarkus.devtools.testing.SnapshotTesting.checkContains;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class QuarkusProjectServiceTest {

    @Inject
    CodeQuarkusConfig codeQuarkusConfig;

    @Inject
    PlatformService platformService;

    @Test
    @DisplayName("When using default project, then, it should create the zip with all the files correctly with the requested content")
    void testDefaultZip(TestInfo info) throws Throwable {
        // When
        QuarkusProjectService creator = getProjectService();
        byte[] proj = creator.create(platformService.recommendedPlatformInfo(), ProjectDefinition.of());
        var testDir = QuarkusProjectServiceTestUtils.extractProject(proj);
        var projDir = Paths.get(testDir.getKey().getPath(), "code-with-quarkus");

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir);

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>org.acme</groupId>"))
            .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
            .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
            .satisfies(checkContains("<quarkus.platform.group-id>%s</quarkus.platform.group-id>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getGroupId())))
            .satisfies(checkContains("<quarkus.platform.artifact-id>%s</quarkus.platform.artifact-id>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getArtifactId())))
            .satisfies(checkContains("<quarkus.platform.version>%s</quarkus.platform.version>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getVersion())))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy-reactive</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"));

        assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
            .satisfies(checkContains("@Path(\"/hello\")"));
    }


    @Test
    @DisplayName("When using default project, then, it should create all the files correctly with the requested content")
    void testDefault(TestInfo info) throws Throwable {
    // When
    QuarkusProjectService creator = getProjectService();
    var projDir = creator.createTmp(platformService.recommendedPlatformInfo(), ProjectDefinition.of());

    // Then
    assertThatDirectoryTreeMatchSnapshots(info, projDir);

    assertThat(projDir.resolve("pom.xml"))
        .satisfies(checkContains("<groupId>org.acme</groupId>"))
        .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
        .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
        .satisfies(checkContains("<quarkus.platform.group-id>%s</quarkus.platform.group-id>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getGroupId())))
        .satisfies(checkContains("<quarkus.platform.artifact-id>%s</quarkus.platform.artifact-id>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getArtifactId())))
        .satisfies(checkContains("<quarkus.platform.version>%s</quarkus.platform.version>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getVersion())))
        .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
        .satisfies(checkContains("<artifactId>quarkus-resteasy-reactive</artifactId>"))
        .satisfies(checkContains("<maven.compiler.release>%s</maven.compiler.release>".formatted(platformService.recommendedPlatformInfo().stream().javaCompatibility().recommended())))
        .satisfies(checkContains("<artifactId>rest-assured</artifactId>"));

    assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
        .satisfies(checkContains("@Path(\"/hello\")"));
    }

    @Test
    @DisplayName("When using 2.16 project, then, it should create all the files correctly with the requested content")
    void test2_16(TestInfo info) throws Throwable {
        // When
        QuarkusProjectService creator = getProjectService();
        PlatformInfo platformInfo = platformService.platformInfo("2.16");
        Path projDir = creator.createTmp(platformInfo, ProjectDefinition.builder().streamKey("2.16").build());

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir);

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>org.acme</groupId>"))
            .satisfies(checkContains("<artifactId>code-with-quarkus</artifactId>"))
            .satisfies(checkContains("<version>1.0.0-SNAPSHOT</version>"))
            .satisfies(checkContains("<quarkus.platform.group-id>%s</quarkus.platform.group-id>".formatted(platformInfo.extensionCatalog().getBom().getGroupId())))
            .satisfies(checkContains("<quarkus.platform.artifact-id>%s</quarkus.platform.artifact-id>".formatted(platformInfo.extensionCatalog().getBom().getArtifactId())))
            .satisfies(checkContains("<quarkus.platform.version>%s</quarkus.platform.version>".formatted(platformInfo.extensionCatalog().getBom().getVersion())))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy-reactive</artifactId>"))
            .satisfies(checkContains("<maven.compiler.release>%s</maven.compiler.release>".formatted(platformService.recommendedPlatformInfo().stream().javaCompatibility().recommended())))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"));

        assertThatMatchSnapshot(info, projDir, "src/main/java/org/acme/GreetingResource.java")
            .satisfies(checkContains("@Path(\"/hello\")"));
    }


    @Test
    @DisplayName("When using a custom project, then, it should create all the files correctly with the requested content")
    void testCustom(TestInfo info) throws Throwable {
        // When
        QuarkusProjectService creator = getProjectService();
        byte[] proj = creator.create(
            platformService.recommendedPlatformInfo(),
            ProjectDefinition.builder().groupId("com.test").artifactId("test-app").version("2.0.0")
                .className("com.test.TestResource").path("/test/it")
                .extensions(
                    Set.of(
                        "io.quarkus:quarkus-resteasy-reactive",
                        "io.quarkus:quarkus-resteasy-reactive-jsonb",
                        "quarkus-neo4j",
                        "hibernate-validator"
                    )
                )
                .javaVersion(platformService.recommendedPlatformInfo().stream().javaCompatibility().recommended())
                .build()
        );
        Entry<File, List<String>> testDir = QuarkusProjectServiceTestUtils.extractProject(proj);
        Path projDir = Paths.get(testDir.getKey().toString(), "test-app");

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir);

        assertThat(projDir.resolve("pom.xml"))
            .satisfies(checkContains("<groupId>com.test</groupId>"))
            .satisfies(checkContains("<artifactId>test-app</artifactId>"))
            .satisfies(checkContains("<version>2.0.0</version>"))
            .satisfies(checkContains("<quarkus.platform.group-id>%s</quarkus.platform.group-id>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getGroupId())))
            .satisfies(checkContains("<quarkus.platform.artifact-id>%s</quarkus.platform.artifact-id>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getArtifactId())))
            .satisfies(checkContains("<quarkus.platform.version>%s</quarkus.platform.version>".formatted(platformService.recommendedPlatformInfo().extensionCatalog().getBom().getVersion())))
            .satisfies(checkContains("<groupId>io.quarkus</groupId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy-reactive</artifactId>"))
            .satisfies(checkContains("<artifactId>quarkus-resteasy-reactive-jsonb</artifactId>"))
            .satisfies(checkContains("<artifactId>quarkus-hibernate-validator</artifactId>"))
            .satisfies(checkContains("<artifactId>quarkus-neo4j</artifactId>"))
            .satisfies(checkContains("<artifactId>rest-assured</artifactId>"))
            .satisfies(checkContains("<maven.compiler.release>%s</maven.compiler.release>".formatted(platformService.recommendedPlatformInfo().stream().javaCompatibility().recommended())));


        assertThatMatchSnapshot(info, projDir, "src/main/java/com/test/TestResource.java")
            .satisfies(checkContains("@Path(\"/test/it\")"));
    }

    @Test
    @DisplayName("Create a Gradle project using kotlin source")
    void testGradleKotlin(TestInfo info) throws Throwable {
        // When
        QuarkusProjectService creator = getProjectService();

        byte[] proj = creator.create(
            platformService.recommendedPlatformInfo(),
            ProjectDefinition.builder()
                .groupId("com.kot")
                .artifactId("test-kotlin-app")
                .version("2.0.0")
                .buildTool("GRADLE")
                .className("com.test.TestResource")
                .extensions(Set.of("resteasy-reactive", "kotlin"))
                .build()
        );
        Entry<File, List<String>> testDir = QuarkusProjectServiceTestUtils.extractProject(proj);
        Path projDir = Paths.get(testDir.getKey().toString(), "test-kotlin-app");

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir);
        assertThatMatchSnapshot(info, projDir, "settings.gradle")
            .satisfies(checkContains("rootProject.name='test-kotlin-app'"));
        assertThat(projDir.resolve("build.gradle"))
            .satisfies(checkContains("id 'org.jetbrains.kotlin.jvm' version "))
            .satisfies(checkContains("implementation 'io.quarkus:quarkus-resteasy-reactive'"))
            .satisfies(checkContains("implementation 'io.quarkus:quarkus-kotlin'"))
            .satisfies(checkContains("implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk"))
            .satisfies(checkContains("group 'com.kot'"))
            .satisfies(checkContains("version '2.0.0'"));

        assertThatMatchSnapshot(info, projDir, "src/main/kotlin/com/test/TestResource.kt")
            .satisfies(checkContains("fun hello() = \"Hello from RESTEasy Reactive\""));
    }

    @Test
    @DisplayName("Create a Gradle project with java 17")
    void testGradle17(TestInfo info) throws IOException, QuarkusCommandException, ArchiveException {
        // When
        QuarkusProjectService creator = getProjectService();

        byte[] proj = creator.create(
            platformService.recommendedPlatformInfo(),
            ProjectDefinition.builder()
                .groupId("com.gr")
                .artifactId("test-gradle-17-app")
                .buildTool("GRADLE")
                .javaVersion(17)
                .build()
        );
        Entry<File, List<String>> testDir = QuarkusProjectServiceTestUtils.extractProject(proj);
        Path projDir = Paths.get(testDir.getKey().toString(), "test-gradle-17-app");

        // Then
        assertThat(projDir.resolve("build.gradle"))
            .satisfies(checkContains("sourceCompatibility = JavaVersion.VERSION_17"))
            .satisfies(checkContains("targetCompatibility = JavaVersion.VERSION_17"));
    }

    @Test
    @DisplayName("Create a Gradle project with java 21")
    void testGradle21(TestInfo info) throws IOException, QuarkusCommandException, ArchiveException {
        // When
        QuarkusProjectService creator = getProjectService();

        byte[] proj = creator.create(
            platformService.recommendedPlatformInfo(),
            ProjectDefinition.builder()
                .groupId("com.gr")
                .artifactId("test-gradle-21-app")
                .buildTool("GRADLE")
                .javaVersion(21)
                .build()
        );
        Entry<File, List<String>> testDir = QuarkusProjectServiceTestUtils.extractProject(proj);
        Path projDir = Paths.get(testDir.getKey().getPath(), "test-gradle-21-app");

        // Then
        assertThat(projDir.resolve("build.gradle"))
            .satisfies(checkContains("sourceCompatibility = JavaVersion.VERSION_21"))
            .satisfies(checkContains("targetCompatibility = JavaVersion.VERSION_21"));
    }

    @Test
    @DisplayName("Create a project with quinoa and YAML config")
    void testQuinoaYaml(TestInfo info) throws Throwable {
        // When
        QuarkusProjectService creator = getProjectService();

        byte[] proj = creator.create(
            platformService.recommendedPlatformInfo(),
            ProjectDefinition.builder()
                .groupId("my.quinoa.yaml.app")
                .artifactId("test-quinoa-yaml-app")
                .buildTool("MAVEN")
                .extensions(Set.of("quinoa", "config-yaml"))
                .build()
        );
        Entry<File, List<String>> testDir = QuarkusProjectServiceTestUtils.extractProject(proj);
        Path projDir = Paths.get(testDir.getKey().toString(), "test-quinoa-yaml-app");

        // Then
        assertThatDirectoryTreeMatchSnapshots(info, projDir)
            .contains(
                "src/main/webui/package.json",
                "src/main/java/my/quinoa/yaml/app/GreetingConfig.java",
                "src/main/resources/application.yml"
            );
    }

    @Test
    @Timeout(2)
    void testMultipleProjects() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        CountDownLatch latch = new CountDownLatch(20);
        QuarkusProjectService creator = getProjectService();
        List<Callable<Void>> creates = IntStream.rangeClosed(1, 20)
            .mapToObj(i -> (Callable<Void>) () -> {
                creator.create(platformService.recommendedPlatformInfo(), ProjectDefinition.builder().build());
                latch.countDown();
                return null;
            })
            .collect(Collectors.toList());

        executorService.invokeAll(creates);
        System.out.println("await");
        latch.await();
        System.out.println("done");
    }

    private QuarkusProjectService getProjectService() {
        return new QuarkusProjectService();
    }
}