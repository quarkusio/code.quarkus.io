package io.quarkus.code.service

import io.quarkus.code.model.ProjectDefinition
import io.quarkus.devtools.codestarts.CodestartException
import io.quarkus.devtools.commands.CreateProject
import io.quarkus.devtools.messagewriter.MessageWriter
import io.quarkus.devtools.project.BuildTool
import io.quarkus.devtools.project.JavaVersion
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.devtools.project.SourceType
import io.quarkus.devtools.project.compress.QuarkusProjectCompress
import jakarta.inject.Singleton
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger

@Singleton
open class QuarkusProjectService {

    open fun create(platformInfo: PlatformInfo, projectDefinition: ProjectDefinition): ByteArray {
        val path = createTmp(platformInfo, projectDefinition)
        val time = System.currentTimeMillis() - 24 * 3600000
        val zipPath = Files.createTempDirectory("zipped-").resolve("project.zip")
        QuarkusProjectCompress.zip(path, zipPath, true, time)
        return Files.readAllBytes(zipPath)
    }

    fun createTmp(
        platformInfo: PlatformInfo,
        projectDefinition: ProjectDefinition,
        isGitHub: Boolean = false,
        silent: Boolean = false
    ): Path {
        val location = Files.createTempDirectory("generated-").resolve(projectDefinition.artifactId)
        createProject(platformInfo, projectDefinition, location, isGitHub, silent)
        return location
    }

    private fun createProject(
        platformInfo: PlatformInfo,
        projectDefinition: ProjectDefinition,
        projectFolderPath: Path,
        gitHub: Boolean,
        silent: Boolean = false
    ) {
        val extensions =
            platformInfo.checkAndMergeExtensions(projectDefinition.extensions)
        val buildTool = BuildTool.valueOf(projectDefinition.buildTool)
        val codestarts = HashSet<String>()
        val javaVersionString = (projectDefinition.javaVersion ?: platformInfo.stream.javaCompatibility.recommended).toString()
        if (gitHub) {
            codestarts.add("tooling-github-action")
        }
        val javaVersion = JavaVersion(javaVersionString)
        if (javaVersion.isPresent && !platformInfo.stream.javaCompatibility.versions.contains(javaVersion.asInt)) {
            throw IllegalArgumentException("This Java version is not compatible with this stream (${platformInfo.stream.javaCompatibility.versions}): $javaVersionString");
        }
        val isJava = extensions.stream()
            .noneMatch{ it.startsWith("io.quarkus:quarkus-kotlin") || it.startsWith("io.quarkus:quarkus-scala") }
        if (javaVersion.isPresent && !isJava && javaVersion.asInt > JavaVersion.MAX_LTS_SUPPORTED_BY_KOTLIN) {
            throw IllegalArgumentException("This Java version is not yet compatible with Kotlin and Scala using Quarkus (max:${JavaVersion.MAX_LTS_SUPPORTED_BY_KOTLIN}): $javaVersionString");
        }
        val messageWriter =
            if (silent) MessageWriter.info(PrintStream(OutputStream.nullOutputStream())) else MessageWriter.info()
        try {
            val project =
                QuarkusProjectHelper.getProject(
                    projectFolderPath,
                    platformInfo.extensionCatalog,
                    buildTool,
                    javaVersion,
                    messageWriter
                )
            val projectDefinition = CreateProject(project)
                .groupId(projectDefinition.groupId)
                .artifactId(projectDefinition.artifactId)
                .version(projectDefinition.version)
                .resourcePath(projectDefinition.path)
                .extraCodestarts(codestarts)
                .javaVersion(javaVersion.version)
                .resourceClassName(projectDefinition.className)
                .extensions(extensions)
                .noCode(projectDefinition.noCode || projectDefinition.noExamples)
            if (platformInfo.quarkusCoreVersion.contains("-redhat-")) {
                // Hack to use the community quarkus gradle plugin (it is not released with the RHBQ)
                projectDefinition.quarkusGradlePluginVersion(platformInfo.quarkusCoreVersion.replace("-redhat-.*".toRegex(), ""))
            }
            val result = projectDefinition.execute()
            if (!result.isSuccess) {
                throw IOException("Error during Quarkus project creation")
            }
        } catch (e: CodestartException) {
            throw IllegalArgumentException(e.message)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(QuarkusProjectService::class.java.name)
    }
}
