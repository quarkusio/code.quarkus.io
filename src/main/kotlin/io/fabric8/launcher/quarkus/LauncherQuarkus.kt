package io.fabric8.launcher.quarkus

import io.fabric8.launcher.quarkus.model.QuarkusProject
import io.quarkus.cli.commands.AddExtensions
import io.quarkus.cli.commands.CreateProject
import io.quarkus.cli.commands.writer.ZipProjectWriter
import io.quarkus.templates.BuildTool
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipOutputStream
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response


@Path("/quarkus")
class LauncherQuarkus {

    @GET
    @Path("/download")
    @Produces("application/zip")
    fun download(@Valid @BeanParam params: QuarkusProject): Response {
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)
        zos.use {
            val zipWrite = ZipProjectWriter(zos)
            zipWrite.use {
                val sourceType = CreateProject.determineSourceType(params.dependencies)
                val success = CreateProject(zipWrite)
                    .groupId(params.groupId)
                    .artifactId(params.artifactId)
                    .version(params.version)
                    .sourceType(sourceType)
                    .buildTool(BuildTool.MAVEN)
                    .className(params.className)
                    .doCreateProject(mutableMapOf())

                if (!success) {
                    throw IOException("Error during Quarkus project creation")
                }
                AddExtensions(zipWrite, "pom.xml")
                    .addExtensions(params.dependencies)
            }
        }
        return Response
            .ok(baos.toByteArray())
            .type("application/zip")
            .header("Content-Disposition", "attachment; filename=\"${params.artifactId}.zip\"")
            .build()

    }

}