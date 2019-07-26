package io.launcher.quarkus

import io.launcher.quarkus.model.QuarkusProject
import io.launcher.quarkus.writer.ZipProjectWriter
import io.quarkus.cli.commands.AddExtensions
import io.quarkus.cli.commands.CreateProject
import io.quarkus.templates.BuildTool
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipOutputStream
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
open class QuarkusProjectCreator {
    companion object {
        const val RESOURCES_DIR = "/launcher/mvnw"
        const val MAVEN_WRAPPER_DIR = ".mvn/wrapper"
        const val MAVEN_WRAPPER_JAR = "$MAVEN_WRAPPER_DIR/maven-wrapper.jar"
        const val MAVEN_WRAPPER_PROPS = "$MAVEN_WRAPPER_DIR/maven-wrapper.properties"
        const val MAVEN_WRAPPER_DOWNLOADER = "$MAVEN_WRAPPER_DIR/MavenWrapperDownloader.java"
        const val MVNW_CMD = "mvnw.cmd"
        const val MVNW = "mvnw"
    }

    fun create(project: QuarkusProject): ByteArray {
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)
        zos.use {
            val zipWrite = ZipProjectWriter(zos, project.artifactId)
            zipWrite.use {
                val sourceType = CreateProject.determineSourceType(project.extensions)
                val success = CreateProject(zipWrite)
                    .groupId(project.groupId)
                    .artifactId(project.artifactId)
                    .version(project.version)
                    .sourceType(sourceType)
                    .buildTool(BuildTool.MAVEN)
                    .className(project.className)
                    .doCreateProject(mutableMapOf())
                if (!success) {
                    throw IOException("Error during Quarkus project creation")
                }
                AddExtensions(zipWrite, "pom.xml")
                    .addExtensions(project.extensions)
                this.addMvnw(zipWrite)
            }
        }
        return baos.toByteArray()
    }

    private fun addMvnw(zipWrite: ZipProjectWriter) {
        zipWrite.mkdirs(MAVEN_WRAPPER_DIR)
        writeResourceFile(zipWrite, MAVEN_WRAPPER_JAR)
        writeResourceFile(zipWrite, MAVEN_WRAPPER_PROPS)
        writeResourceFile(zipWrite, MAVEN_WRAPPER_DOWNLOADER)
        writeResourceFile(zipWrite, MVNW_CMD)
        writeResourceFile(zipWrite, MVNW)
    }

    private fun writeResourceFile(zipWrite: ZipProjectWriter, filePath: String) {
        if (!zipWrite.exists(filePath)) {
            val resourcePath = "$RESOURCES_DIR/$filePath"
            val resource = QuarkusProjectCreator::class.java.getResource(resourcePath)
                ?: throw IOException("missing resource $resourcePath")
            val fileAsBytes =
                resource.readBytes()
            zipWrite.write(filePath, fileAsBytes)
        }
    }

}