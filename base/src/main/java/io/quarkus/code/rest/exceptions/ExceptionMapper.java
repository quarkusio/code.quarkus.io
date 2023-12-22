package io.quarkus.code.rest.exceptions;

import io.quarkus.logging.Log;
import io.quarkus.runtime.LaunchMode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@ApplicationScoped
public class ExceptionMapper {

    @ServerExceptionMapper(priority = Priorities.USER + 2)
    public Uni<Response> mapException(Throwable exception) {
        if (Log.isDebugEnabled() || LaunchMode.current().isDevOrTest()) {
            Log.error(exception.getMessage(), exception);
        }
        if(exception instanceof WebApplicationException) {
            return Uni.createFrom().item(((WebApplicationException) exception).getResponse());
        }
        return Uni.createFrom().item(Response.serverError().build());
    }

}