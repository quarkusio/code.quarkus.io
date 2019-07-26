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
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/quarkus")
class LauncherQuarkusEndpoints {

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

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
        return Response
            .ok(projectCreator.create(params))
            .type("application/zip")
            .header("Content-Disposition", "attachment; filename=\"${params.artifactId}.zip\"")
            .build()

    }

}