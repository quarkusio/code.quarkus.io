package io.quarkus.code.rest

import io.vertx.core.http.HttpHeaders
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider


@Provider
class CORSFilter : ContainerRequestFilter {

    @Context
    internal var info: UriInfo? = null

    @Context
    var request: HttpServletRequest? = null

    @Context
    var response: HttpServletResponse? = null

    override fun filter(context: ContainerRequestContext) {
        val origin = request?.getHeader(HttpHeaders.ORIGIN.toString()) ?: return

        response?.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), origin)

        val requestedMethods = request?.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD.toString())
        if (requestedMethods != null) {
            response?.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS.toString(), requestedMethods)
        }

        val requestedHeaders = request?.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS.toString())
        if (requestedHeaders != null) {
            response?.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS.toString(), requestedHeaders)
        }
    }
}