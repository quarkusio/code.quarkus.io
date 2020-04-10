package io.quarkus.code.gen

import io.quarkus.cli.commands.AddExtensions
import io.quarkus.cli.commands.CreateProject
import io.quarkus.cli.commands.writer.FileProjectWriter
import io.quarkus.cli.commands.writer.ProjectWriter
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.gen.writer.CommonsZipProjectWriter
import io.quarkus.code.extensions.QuarkusExtensionCatalog
import io.quarkus.generators.BuildTool
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarkusProjectCreator {
    companion object {
        private const val MVNW_RESOURCES_DIR = "/creator/mvnw"
        private const val MVNW_WRAPPER_DIR = ".mvn/wrapper"
        private const val MVNW_WRAPPER_JAR = "$MVNW_WRAPPER_DIR/maven-wrapper.jar"
        private const val MVNW_WRAPPER_PROPS = "$MVNW_WRAPPER_DIR/maven-wrapper.properties"
        private const val MVNW_WRAPPER_DOWNLOADER = "$MVNW_WRAPPER_DIR/MavenWrapperDownloader.java"
        private const val MVNW_CMD = "mvnw.cmd"
        private const val MVNW = "mvnw"

        // Gradlew constants
        private const val GRADLEW_RESOURCES_DIR = "/creator/gradlew"
        private const val GRADLEW_WRAPPER_DIR = "gradle/wrapper"
        private const val GRADLEW_WRAPPER_JAR = "$GRADLEW_WRAPPER_DIR/gradle-wrapper.jar"
        private const val GRADLEW_WRAPPER_PROPS = "$GRADLEW_WRAPPER_DIR/gradle-wrapper.properties"
        private const val GRADLEW_BAT = "gradlew.bat"
        private const val GRADLEW = "gradlew"
    }

    @Inject
    internal lateinit var extensionCatalog: QuarkusExtensionCatalog

    fun create(project: QuarkusProject): ByteArray {
        QuarkusExtensionCatalog.checkPlatformInitialization()
        val baos = ByteArrayOutputStream()
        baos.use {
            val zipWriter = CommonsZipProjectWriter.createWriter(baos, project.artifactId)
            zipWriter.use {
                createProject(project, zipWriter)
            }
        }
        return baos.toByteArray()
    }

    fun createTmp(project: QuarkusProject): Path {
        val location = Files.createTempDirectory("generated-")
        val fileProjectWriter = FileProjectWriter(location.toFile())
        createProject(project, fileProjectWriter)
        return location;
    }

    private fun createProject(project: QuarkusProject, projectWriter: ProjectWriter) {
        val extensions = checkAndMergeExtensions(project)
        val sourceType = CreateProject.determineSourceType(extensions)
        val context = mutableMapOf("path" to (project.path as Any))
        val buildTool = io.quarkus.generators.BuildTool.valueOf(project.buildTool)
        val success = CreateProject(projectWriter, QuarkusExtensionCatalog.descriptor)
                .groupId(project.groupId)
                .artifactId(project.artifactId)
                .version(project.version)
                .sourceType(sourceType)
                .buildTool(buildTool)
                .className(project.className)
                .javaTarget("11")
                .doCreateProject(context)
        if (!success) {
            throw IOException("Error during Quarkus project creation")
        }
        AddExtensions(projectWriter, buildTool, QuarkusExtensionCatalog.descriptor)
                .extensions(extensions)
                .execute()
        if (buildTool == BuildTool.MAVEN) {
            addMvnw(projectWriter)
        } else if (buildTool == BuildTool.GRADLE) {
            addGradlew(projectWriter)
        }
    }

    private fun checkAndMergeExtensions(project: QuarkusProject): Set<String> {
        return extensionCatalog.checkAndMergeExtensions(project.extensions, project.shortExtensions)
    }

    private fun addMvnw(projectWriter: ProjectWriter) {
        projectWriter.mkdirs(MVNW_WRAPPER_DIR)
        writeResourceFile(projectWriter, MVNW_RESOURCES_DIR, MVNW_WRAPPER_JAR)
        writeResourceFile(projectWriter, MVNW_RESOURCES_DIR, MVNW_WRAPPER_PROPS)
        writeResourceFile(projectWriter, MVNW_RESOURCES_DIR, MVNW_WRAPPER_DOWNLOADER)
        writeResourceFile(projectWriter, MVNW_RESOURCES_DIR, MVNW_CMD, true)
        writeResourceFile(projectWriter, MVNW_RESOURCES_DIR, MVNW, true)
    }

    private fun addGradlew(projectWriter: ProjectWriter) {
        projectWriter.mkdirs(GRADLEW_WRAPPER_DIR)
        writeResourceFile(projectWriter, GRADLEW_RESOURCES_DIR, GRADLEW_WRAPPER_JAR)
        writeResourceFile(projectWriter, GRADLEW_RESOURCES_DIR, GRADLEW_WRAPPER_PROPS)
        writeResourceFile(projectWriter, GRADLEW_RESOURCES_DIR, GRADLEW_BAT, true)
        writeResourceFile(projectWriter, GRADLEW_RESOURCES_DIR, GRADLEW, true)
    }

    private fun writeResourceFile(projectWriter: ProjectWriter, resourcesDir: String, filePath: String, allowExec: Boolean = false) {
        if (!projectWriter.exists(filePath)) {
            val resourcePath = "$resourcesDir/$filePath"
            val resource = QuarkusProjectCreator::class.java.getResource(resourcePath)
                    ?: throw IOException("missing resource $resourcePath")
            val fileAsBytes = resource.readBytes()

            if (projectWriter is CommonsZipProjectWriter){
                projectWriter.write(filePath, fileAsBytes, allowExec)
            } else {
                projectWriter.write(filePath, resource.file)
            }
        }
    }

}
