package io.quarkus.code

import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.model.ShortUrl
import org.eclipse.microprofile.openapi.annotations.Operation
import java.net.URI
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/s")
class ShortUrlResource {
    @Inject
    lateinit var urlRepository: UrlRepository

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Create a short url based on the parameters")
    fun createShort(@Valid @BeanParam project: QuarkusProject): Response {
        val url = "https://code.quarkus.io/api/download?g=${project.groupId}&a=${project.artifactId}&v=${project.version}&c=${project.className}&e=${project.extensions}"
        val response = { shortUrl: ShortUrl ->
            Response.ok("https://code.quarkus.io/s/${shortUrl.id}").build()
        }
        urlRepository.getByUrl(url)?.let { shortUrl ->
            return response(shortUrl)
        }
        val shortUrl = ShortUrl(url = url)
        urlRepository.save(shortUrl)

        return response(shortUrl)
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Redirect user to download for this id")
    fun getShort(@PathParam("id") id: String): Response {
        val shortUrl = urlRepository.getById(id)
        shortUrl?.url?.let { url ->
            return Response.seeOther(URI(url)).build()
        }
        return Response.status(404).entity("This link has expired").build()
    }

}