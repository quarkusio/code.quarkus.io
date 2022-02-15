package io.quarkus.code.service

import io.quarkus.code.model.ProjectDefinition
import io.quarkus.devtools.codestarts.CodestartException
import io.quarkus.devtools.commands.CreateProject
import io.quarkus.devtools.commands.data.QuarkusCommandException
import io.quarkus.devtools.messagewriter.MessageWriter
import io.quarkus.devtools.project.BuildTool
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.devtools.project.codegen.CreateProjectHelper
import io.quarkus.devtools.project.compress.QuarkusProjectCompress
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger
import javax.inject.Singleton

@Singleton
class QuarkusProjectService {

    fun create(platformInfo: PlatformInfo, projectDefinition: ProjectDefinition): ByteArray {
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
        val sourceType = CreateProjectHelper.determineSourceType(extensions)
        val buildTool = BuildTool.valueOf(projectDefinition.buildTool)
        val codestarts = HashSet<String>()
        if (gitHub) {
            codestarts.add("github-action")
        }
        if (projectDefinition.noExamples) {
            LOG.warning("Use of @Deprecated ProjectDefinition.noExamples (ne)")
        }
        val messageWriter =
            if (silent) MessageWriter.info(PrintStream(OutputStream.nullOutputStream())) else MessageWriter.info()
        try {
            val project =
                QuarkusProjectHelper.getProject(
                    projectFolderPath,
                    platformInfo.extensionCatalog,
                    buildTool,
                    messageWriter
                )
            val projectDefinition = CreateProject(project)
                .groupId(projectDefinition.groupId)
                .artifactId(projectDefinition.artifactId)
                .version(projectDefinition.version)
                .sourceType(sourceType)
                .resourcePath(projectDefinition.path)
                .extraCodestarts(codestarts)
                .javaTarget(projectDefinition.javaVersion)
                .className(projectDefinition.className)
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
        } catch (e: QuarkusCommandException) {
            throw IOException("Error during Quarkus project creation", e)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(QuarkusProjectService::class.java.name)
    }
}
