package io.quarkus.code.rest

import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class IllegalArgumentExceptionMapper: ExceptionMapper<IllegalArgumentException>  {
    override fun toResponse(e: IllegalArgumentException): Response {
        val message = "Bad request > ${e.message}"
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(message)
            .type(TEXT_PLAIN)
            .build()
    }
}