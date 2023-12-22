package io.quarkus.code.rest.exceptions;

import io.quarkus.devtools.codestarts.CodestartStructureException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CodestartStructureExceptionMapper implements ExceptionMapper<CodestartStructureException> {
    @Override
    public Response toResponse(CodestartStructureException e) {
        String message = "Codestart structure error > " + e.getMessage();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
