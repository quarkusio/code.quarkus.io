package io.quarkus.code.analytics

import io.quarkus.code.services.QuarkusExtensionCatalog
import java.util.logging.Level
import java.util.logging.Logger
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

    companion object {
        private val log = Logger.getLogger(AnalyticsFilter::class.java.name)
    }

    @Inject
    lateinit var googleAnalyticsService: GoogleAnalyticsService

    @Inject
    lateinit var extensionCatalog: QuarkusExtensionCatalog

    @Context
    internal var info: UriInfo? = null

    @Context
    var httpRequest: HttpServletRequest? = null

    override fun filter(context: ContainerRequestContext) {
        try {
            val queryParams = context.uriInfo.queryParameters
            val path = info!!.path
            val applicationName = queryParams.getFirst("cn") ?: context.getHeaderString("Client-Name") ?: "unknown"
            val url = info!!.requestUri.toString()
            val userAgent = context.headers.getFirst(HttpHeaders.USER_AGENT)
            val referer = context.headers.getFirst("Referer")
            val remoteAddr = httpRequest!!.remoteAddr
            val extensions: Set<String>?
            val buildTool: String?
            if (path.startsWith("/download")) {
                try {
                    extensions = extensionCatalog.checkAndMergeExtensions(queryParams["e"]?.toSet(), queryParams.getFirst("s"))
                    buildTool = queryParams.getFirst("b") ?: "MAVEN".toUpperCase()
                    extensions.forEach {
                        googleAnalyticsService.sendEvent(
                                category = "Extension",
                                action = "Used",
                                label = it,
                                applicationName = applicationName,
                                path = path,
                                url = url,
                                userAgent = userAgent,
                                referer = referer,
                                remoteAddr = remoteAddr,
                                extensions = extensions,
                                buildTool = buildTool
                        )
                    }
                    googleAnalyticsService.sendEvent(
                            category = "App",
                            action = "Download",
                            label = applicationName,
                            applicationName = applicationName,
                            path = path,
                            url = url,
                            userAgent = userAgent,
                            referer = referer,
                            remoteAddr = remoteAddr,
                            extensions = extensions,
                            buildTool = buildTool
                    )

                } catch (e: IllegalStateException) {
                    log.log(Level.FINE, e) { "Error while extracting extension list from request" }
                }
            }
            googleAnalyticsService.sendEvent(
                    category = "API",
                    action = path,
                    label = applicationName,
                    applicationName = applicationName,
                    path = path,
                    url = url,
                    userAgent = userAgent,
                    referer = referer,
                    remoteAddr = remoteAddr,
                    extensions = null,
                    buildTool = null
            )
        } catch (e: Exception) {
            log.log(Level.SEVERE, e) { "Error while generating/sending an analytic event" }
        }

    }
}