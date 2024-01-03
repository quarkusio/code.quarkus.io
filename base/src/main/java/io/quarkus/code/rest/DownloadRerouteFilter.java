package io.quarkus.code.rest;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DownloadRerouteFilter {

    public void init(@Observes Router router) {
        router.route(HttpMethod.GET, "/d").handler(ctx -> {
            ctx.reroute(ctx.request().uri().replace("/d", "/api/download"));
        });
    }
}