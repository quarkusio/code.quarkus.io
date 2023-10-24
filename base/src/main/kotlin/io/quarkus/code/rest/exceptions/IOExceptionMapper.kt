package io.quarkus.code.rest.exceptions

import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import java.io.IOException

@Provider
class IOExceptionMapper: ExceptionMapper<IOException>  {
    override fun toResponse(e: IOException): Response {
        val message = "IO error > ${e.message}"
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(message)
            .type(TEXT_PLAIN)
            .build()
    }
}