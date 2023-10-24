package io.quarkus.code.rest.exceptions

import io.quarkus.devtools.commands.data.QuarkusCommandException
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class QuarkusCommandExceptionMapper: ExceptionMapper<QuarkusCommandException>  {
    override fun toResponse(e: QuarkusCommandException): Response {
        val message = "Quarkus Command error > ${e.localizedMessage}"
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(message)
            .type(TEXT_PLAIN)
            .build()
    }
}