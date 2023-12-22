package io.quarkus.code.rest.exceptions;

import io.quarkus.devtools.commands.data.QuarkusCommandException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuarkusCommandExceptionMapper implements ExceptionMapper<QuarkusCommandException> {
    @Override
    public Response toResponse(QuarkusCommandException e) {
        String message = "Quarkus Command error > " + e.getLocalizedMessage();
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}