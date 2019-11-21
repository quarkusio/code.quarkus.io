package io.quarkus.code.analytics

import java.util.stream.Collectors
import javax.inject.Inject
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider

@Provider
class AnalyticsFilter : ContainerRequestFilter {

    @Inject
    lateinit var googleAnalyticsService: GoogleAnalyticsService

    @Context
    internal var info: UriInfo? = null

    override fun filter(context: ContainerRequestContext) {
        val stream = context.uriInfo.queryParameters.entries.stream()
        val params = stream.map { entry -> "${entry.key}=${entry.value}" }.collect(Collectors.joining("&"))
        val path = info!!.path
        val clientName = context.getHeaderString("Client-Name")
        googleAnalyticsService.sendEvent(clientName, path, params)
    }
}