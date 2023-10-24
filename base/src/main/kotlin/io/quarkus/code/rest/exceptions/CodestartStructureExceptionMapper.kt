package io.quarkus.code.rest.exceptions

import io.quarkus.devtools.codestarts.CodestartStructureException
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class CodestartStructureExceptionMapper: ExceptionMapper<CodestartStructureException>  {
    override fun toResponse(e: CodestartStructureException): Response {
        val message = "Codestart structure error > ${e.message}"
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(message)
            .type(TEXT_PLAIN)
            .build()
    }
}