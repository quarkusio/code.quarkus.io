package io.quarkus.code.rest.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class IOExceptionMapper implements ExceptionMapper<IOException> {
    @Override
    public Response toResponse(IOException e) {
        String message = "IO error > " + e.getMessage();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}