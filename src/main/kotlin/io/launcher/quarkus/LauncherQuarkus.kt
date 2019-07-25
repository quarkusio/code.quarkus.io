package io.launcher.quarkus

import io.launcher.quarkus.model.Config
import io.launcher.quarkus.model.QuarkusProject
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
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/quarkus")
class LauncherQuarkus {

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    fun config(): Config {
        return Config(
            System.getenv("LAUNCHER_QUARKUS_ENV") ?: "dev",
            System.getenv("LAUNCHER_QUARKUS_GA_TRACKING_ID") ?: null,
            System.getenv("LAUNCHER_QUARKUS_SENTRY_DSN") ?: null
        )
    }

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