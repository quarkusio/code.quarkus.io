package io.quarkus.code

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.Config
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.io.IOException
import java.net.URI
import java.util.*
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.MediaType.TEXT_PLAIN
import javax.ws.rs.core.Response

@Path("/")
class CodeQuarkusResource {

    @Inject
    lateinit var configManager: CodeQuarkusConfigManager

    @Inject
    lateinit var extensionCatalog: QuarkusExtensionCatalog

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @Inject
    lateinit var urlRepository: UrlRepository

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration (DEPRECATED to '/v1/...')", hidden = true)
    fun config(): Config {
        return configManager.getConfig()
    }


    @GET
    @Path("/extensions")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher list of Quarkus extensions (DEPRECATED to '/v1/...')")
    @APIResponse(
            responseCode = "200",
            description = "List of Quarkus extensions",
            content = [Content(
                    mediaType = APPLICATION_JSON,
                    schema = Schema(implementation = CodeQuarkusExtension::class)
            )]
    )
    fun extensions(): List<CodeQuarkusExtension> {
        return extensionCatalog.extensions
    }

    @GET
    @Path("/shorten")
    @Produces(TEXT_PLAIN)
    @Operation(summary = "Create a short url based on the parameters")
    fun createShort(@Valid @BeanParam project: QuarkusProject): Response {
        val url = "https://code.quarkus.io/api/download?g=${project.groupId}&a=${project.artifactId}&v=${project.version}&c=${project.className}&e=${project.extensions}"
        val response = { id: String ->
            Response.ok("https://code.quarkus.io/api/shorten/$id").build()
        }
        urlRepository.getByUrl(url)?.let { shortUrl ->
            return response(shortUrl.id)
        }
        val id = UUID.randomUUID().toString()
        val shortUrl = ShortUrl(id, url)
        urlRepository.save(shortUrl)

        return response(id)
    }

    @GET
    @Path("/shorten/{id}")
    @Operation(summary = "Redirect user to download for this id")
    fun getShort(@PathParam("id") id: String): Response {
        val shortUrl = urlRepository.getById(id)
        shortUrl?.url?.let { url ->
            return Response.seeOther(URI(url)).build()
        }
        return Response.serverError().build()
    }


    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings (DEPRECATED to '/v1/...')")
    fun download(@Valid @BeanParam project: QuarkusProject): Response {
        return Response
                .ok(projectCreator.create(project))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"${project.artifactId}.zip\"")
                .build()
    }
}