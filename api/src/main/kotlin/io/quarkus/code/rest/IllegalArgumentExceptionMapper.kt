package io.quarkus.code.rest

import javax.ws.rs.core.MediaType.TEXT_PLAIN
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

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