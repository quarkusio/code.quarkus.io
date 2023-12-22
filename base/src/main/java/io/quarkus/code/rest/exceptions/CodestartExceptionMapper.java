package io.quarkus.code.rest.exceptions;

import io.quarkus.devtools.codestarts.CodestartException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CodestartExceptionMapper implements ExceptionMapper<CodestartException> {
    @Override
    public Response toResponse(CodestartException e) {
        String message = "Bad request > " + e.getMessage();
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}