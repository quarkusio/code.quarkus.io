package io.quarkus.code.rest.exceptions

import io.quarkus.devtools.codestarts.CodestartException
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class CodestartExceptionMapper: ExceptionMapper<CodestartException>  {
    override fun toResponse(e: CodestartException): Response {
        val message = "Bad request > ${e.message}"
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(message)
            .type(TEXT_PLAIN)
            .build()
    }
}