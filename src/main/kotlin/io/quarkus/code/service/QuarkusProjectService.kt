package io.quarkus.code.service

import io.quarkus.code.model.ProjectDefinition
import io.quarkus.devtools.commands.CreateProject
import io.quarkus.devtools.project.BuildTool
import io.quarkus.devtools.project.QuarkusProject
import io.quarkus.devtools.project.compress.QuarkusProjectCompress
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarkusProjectService {
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
    internal lateinit var extensionCatalog: QuarkusExtensionCatalogService

    fun create(projectDefinition: ProjectDefinition): ByteArray {
        QuarkusExtensionCatalogService.checkPlatformInitialization()
        val path = createTmp(projectDefinition)
        val time = System.currentTimeMillis() - 24 * 3600000
        val zipPath = Files.createTempDirectory("zipped-").resolve("project.zip")
        QuarkusProjectCompress.zip(path, zipPath, true, time)
        return Files.readAllBytes(zipPath)
    }

    fun createTmp(projectDefinition: ProjectDefinition): Path {
        val location = Files.createTempDirectory("generated-").resolve(projectDefinition.artifactId)
        createProject(projectDefinition, location)
        return location;
    }

    private fun createProject(projectDefinition: ProjectDefinition, projectFolderPath: Path) {
        val extensions = checkAndMergeExtensions(projectDefinition)
        val sourceType = CreateProject.determineSourceType(extensions)
        val context = mutableMapOf("path" to (projectDefinition.path as Any))
        val buildTool = BuildTool.valueOf(projectDefinition.buildTool)
        val success = CreateProject(projectFolderPath, QuarkusExtensionCatalogService.descriptor)
                .groupId(projectDefinition.groupId)
                .artifactId(projectDefinition.artifactId)
                .version(projectDefinition.version)
                .sourceType(sourceType)
                .buildTool(buildTool)
                .className(projectDefinition.className)
                .javaTarget("11")
                .extensions(extensions)
                .doCreateProject(context)
        if (!success) {
            throw IOException("Error during Quarkus project creation")
        }
        if (buildTool == BuildTool.MAVEN) {
            addMvnw(projectFolderPath)
        } else if (buildTool == BuildTool.GRADLE) {
            addGradlew(projectFolderPath)
        }
    }

    private fun checkAndMergeExtensions(projectDefinition: ProjectDefinition): Set<String> {
        return extensionCatalog.checkAndMergeExtensions(projectDefinition.extensions, projectDefinition.shortExtensions)
    }

    private fun addMvnw(projectFolderPath: Path) {
        Files.createDirectories(projectFolderPath.resolve(MVNW_WRAPPER_DIR))
        writeResourceFile(projectFolderPath, MVNW_RESOURCES_DIR, MVNW_WRAPPER_JAR)
        writeResourceFile(projectFolderPath, MVNW_RESOURCES_DIR, MVNW_WRAPPER_PROPS)
        writeResourceFile(projectFolderPath, MVNW_RESOURCES_DIR, MVNW_WRAPPER_DOWNLOADER)
        writeResourceFile(projectFolderPath, MVNW_RESOURCES_DIR, MVNW_CMD, true)
        writeResourceFile(projectFolderPath, MVNW_RESOURCES_DIR, MVNW, true)
    }

    private fun addGradlew(projectFolderPath: Path) {
        Files.createDirectories(projectFolderPath.resolve(GRADLEW_WRAPPER_DIR))
        writeResourceFile(projectFolderPath, GRADLEW_RESOURCES_DIR, GRADLEW_WRAPPER_JAR)
        writeResourceFile(projectFolderPath, GRADLEW_RESOURCES_DIR, GRADLEW_WRAPPER_PROPS)
        writeResourceFile(projectFolderPath, GRADLEW_RESOURCES_DIR, GRADLEW_BAT, true)
        writeResourceFile(projectFolderPath, GRADLEW_RESOURCES_DIR, GRADLEW, true)
    }

    private fun writeResourceFile(projectFolderPath: Path, resourcesDir: String, filePath: String, allowExec: Boolean = false) {
        val absoluteFilePath = projectFolderPath.resolve(filePath);
        if (!absoluteFilePath.toFile().exists()) {
            val resourcePath = "$resourcesDir/$filePath"
            val resource = QuarkusProjectService::class.java.getResource(resourcePath)
                    ?: throw IOException("missing resource $resourcePath")
            val fileAsBytes = resource.readBytes()
            Files.write(absoluteFilePath, fileAsBytes)
            if(allowExec) {
                Files.setPosixFilePermissions(absoluteFilePath, PosixFilePermissions.fromString("rwxr-xr-x"))
            }
        }
    }

}
