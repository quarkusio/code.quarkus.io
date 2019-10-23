package io.quarkus.code

import io.quarkus.cli.commands.AddExtensions
import io.quarkus.cli.commands.CreateProject
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.writer.CommonsZipProjectWriter
import io.quarkus.generators.BuildTool
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Singleton

@Singleton
open class QuarkusProjectCreator {
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

    open fun create(project: QuarkusProject): ByteArray {
        val baos = ByteArrayOutputStream()
        baos.use {
            val zipWriter = CommonsZipProjectWriter.createWriter(baos, project.artifactId)
            zipWriter.use {
                val sourceType = CreateProject.determineSourceType(project.extensions)
                val context = mutableMapOf("path" to (project.path as Any))
                val buildTool = io.quarkus.generators.BuildTool.valueOf(project.buildTool)
                val success = CreateProject(zipWriter)
                        .groupId(project.groupId)
                        .artifactId(project.artifactId)
                        .version(project.version)
                        .sourceType(sourceType)
                        .buildTool(buildTool)
                        .className(project.className)
                        .extensions(project.extensions)
                        .doCreateProject(context)
                if (!success) {
                    throw IOException("Error during Quarkus project creation")
                }
                AddExtensions(zipWriter, buildTool)
                        .addExtensions(project.extensions)
                if (buildTool == BuildTool.MAVEN) {
                    addMvnw(zipWriter)
                } else if (buildTool == BuildTool.GRADLE) {
                    addGradlew(zipWriter)
                }
            }
        }
        return baos.toByteArray()
    }

    private fun addMvnw(zipWrite: CommonsZipProjectWriter) {
        zipWrite.mkdirs(MVNW_WRAPPER_DIR)
        writeResourceFile(zipWrite, MVNW_RESOURCES_DIR, MVNW_WRAPPER_JAR)
        writeResourceFile(zipWrite, MVNW_RESOURCES_DIR, MVNW_WRAPPER_PROPS)
        writeResourceFile(zipWrite, MVNW_RESOURCES_DIR, MVNW_WRAPPER_DOWNLOADER)
        writeResourceFile(zipWrite, MVNW_RESOURCES_DIR, MVNW_CMD, true)
        writeResourceFile(zipWrite, MVNW_RESOURCES_DIR, MVNW, true)
    }

    private fun addGradlew(zipWrite: CommonsZipProjectWriter) {
        zipWrite.mkdirs(GRADLEW_WRAPPER_DIR)
        writeResourceFile(zipWrite, GRADLEW_RESOURCES_DIR, GRADLEW_WRAPPER_JAR)
        writeResourceFile(zipWrite, GRADLEW_RESOURCES_DIR, GRADLEW_WRAPPER_PROPS)
        writeResourceFile(zipWrite, GRADLEW_RESOURCES_DIR, GRADLEW_BAT, true)
        writeResourceFile(zipWrite, GRADLEW_RESOURCES_DIR, GRADLEW, true)
    }

    private fun writeResourceFile(zipWrite: CommonsZipProjectWriter, resourcesDir: String, filePath: String, allowExec: Boolean = false) {
        if (!zipWrite.exists(filePath)) {
            val resourcePath = "$resourcesDir/$filePath"
            val resource = QuarkusProjectCreator::class.java.getResource(resourcePath)
                    ?: throw IOException("missing resource $resourcePath")
            val fileAsBytes =
                    resource.readBytes()
            zipWrite.write(filePath, fileAsBytes, allowExec)
        }
    }

}