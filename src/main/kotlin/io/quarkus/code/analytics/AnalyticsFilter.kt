package io.quarkus.code.analytics

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider


@Provider
class AnalyticsFilter : ContainerRequestFilter {

    @Inject
    lateinit var googleAnalyticsService: GoogleAnalyticsService

    @Context
    internal var info: UriInfo? = null

    @Context
    var httpRequest: HttpServletRequest? = null

    override fun filter(context: ContainerRequestContext) {
        val queryParams = context.uriInfo.queryParameters
        val path = info!!.path
        val clientName = queryParams.getFirst("cn") ?: context.getHeaderString("Client-Name")
        googleAnalyticsService.sendEvent(
                clientName = clientName,
                path = path,
                url = info!!.requestUri.toString(),
                userAgent = context.headers.getFirst(HttpHeaders.USER_AGENT),
                referer = context.headers.getFirst("Referer"),
                host = context.headers.getFirst(HttpHeaders.HOST),
                remoteAddr = httpRequest!!.remoteAddr
        )
    }
}