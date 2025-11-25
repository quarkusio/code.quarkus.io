package io.quarkus.code.service;

import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.devtools.codestarts.CodestartException;
import io.quarkus.devtools.commands.CreateProject;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.devtools.commands.data.QuarkusCommandOutcome;
import io.quarkus.devtools.messagewriter.MessageWriter;
import io.quarkus.devtools.project.BuildTool;
import io.quarkus.devtools.project.JavaVersion;
import io.quarkus.devtools.project.QuarkusProject;
import io.quarkus.devtools.project.QuarkusProjectHelper;

import io.quarkus.devtools.project.compress.QuarkusProjectCompress;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Singleton
public class QuarkusProjectService {

    @Inject
    Instance<PlatformOverride> platformOverride;

    public byte[] create(PlatformInfo platformInfo, ProjectDefinition projectDefinition)
            throws IOException, QuarkusCommandException {
        Path path = createTmp(platformInfo, projectDefinition);
        long time = System.currentTimeMillis() - 24 * 3600000;
        Path zipPath = Files.createTempDirectory("zipped-").resolve("project.zip");

        QuarkusProjectCompress.zip(path, zipPath, true, time);
        return Files.readAllBytes(zipPath);
    }

    public Path createTmp(
            PlatformInfo platformInfo,
            ProjectDefinition projectDefinition) throws IOException, QuarkusCommandException {
        return this.createTmp(platformInfo, projectDefinition, false, false);
    }

    public Path createTmp(
            PlatformInfo platformInfo,
            ProjectDefinition projectDefinition,
            boolean isGitHub) throws IOException, QuarkusCommandException {
        return this.createTmp(platformInfo, projectDefinition, isGitHub, false);
    }

    public Path createTmp(
            PlatformInfo platformInfo,
            ProjectDefinition projectDefinition,
            boolean isGitHub,
            boolean silent) throws IOException, QuarkusCommandException {
        Path location = Files.createTempDirectory("generated-").resolve(projectDefinition.artifactId());
        createProject(platformInfo, projectDefinition, location, isGitHub, silent);
        if (platformOverride != null && platformOverride.isResolvable()) {
            platformOverride.get().onNewProject(projectDefinition, location);
        }
        return location;
    }

    private void createProject(
            PlatformInfo platformInfo,
            ProjectDefinition projectDefinition,
            Path projectFolderPath,
            boolean gitHub,
            boolean silent) throws IOException, QuarkusCommandException {
        Set<String> extensions = platformInfo.checkAndMergeExtensions(projectDefinition.extensions());
        BuildTool buildTool = BuildTool.valueOf(projectDefinition.buildTool());
        HashSet<String> codestarts = new HashSet<>();
        String javaVersionString = Integer.toString(projectDefinition.javaVersion() != null ? projectDefinition.javaVersion()
                : platformInfo.stream().javaCompatibility().recommended());
        if (gitHub) {
            codestarts.add("tooling-github-action");
        }
        JavaVersion javaVersion = new JavaVersion(javaVersionString);
        if (javaVersion.isPresent() && !platformInfo.stream().javaCompatibility().versions().contains(javaVersion.getAsInt())) {
            throw new IllegalArgumentException("This Java version is not compatible with this stream ("
                    + platformInfo.stream().javaCompatibility().versions() + "): " + javaVersionString);
        }
        boolean isJava = extensions.stream()
                .noneMatch(it -> it.startsWith("io.quarkus:quarkus-kotlin") || it.startsWith("io.quarkus:quarkus-scala"));
        if (javaVersion.isPresent() && !isJava && javaVersion.getAsInt() > JavaVersion.MAX_LTS_SUPPORTED_BY_KOTLIN) {
            throw new IllegalArgumentException(
                    "This Java version is not yet compatible with Kotlin and Scala using Quarkus (max:"
                            + JavaVersion.MAX_LTS_SUPPORTED_BY_KOTLIN + "): " + javaVersionString);
        }
        MessageWriter messageWriter = silent ? MessageWriter.info(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        })) : MessageWriter.info();
        try {
            QuarkusProject project = QuarkusProjectHelper.getProject(
                    projectFolderPath,
                    platformInfo.extensionCatalog(),
                    buildTool,
                    javaVersion,
                    messageWriter);
            CreateProject projectDefinitionCreateProject = new CreateProject(project)
                    .groupId(projectDefinition.groupId())
                    .artifactId(projectDefinition.artifactId())
                    .version(projectDefinition.version())
                    .resourcePath(projectDefinition.path())
                    .extraCodestarts(codestarts)
                    .javaVersion(javaVersion.getVersion())
                    .resourceClassName(projectDefinition.className())
                    .extensions(extensions)
                    .noCode(projectDefinition.noCode() || projectDefinition.noExamples());
            if (platformInfo.quarkusCoreVersion().contains("-redhat-")) {
                // Hack to use the community quarkus gradle plugin (it is not released with the RHBQ)
                projectDefinitionCreateProject
                        .quarkusGradlePluginVersion(platformInfo.quarkusCoreVersion().replace("-redhat-.*", ""));
            }
            QuarkusCommandOutcome result = projectDefinitionCreateProject.execute();
            if (!result.isSuccess()) {
                throw new IOException("Error during Quarkus project creation");
            }
        } catch (CodestartException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
