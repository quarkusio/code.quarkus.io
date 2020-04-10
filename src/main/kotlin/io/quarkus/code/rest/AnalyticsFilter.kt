package io.quarkus.code.rest

import io.quarkus.code.service.GoogleAnalyticsService
import io.quarkus.code.service.QuarkusExtensionCatalogService
import java.util.logging.Level
import java.util.logging.Logger
import javax.enterprise.inject.Instance
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
    lateinit var googleAnalyticsService: Instance<GoogleAnalyticsService>

    @Inject
    lateinit var extensionCatalog: Instance<QuarkusExtensionCatalogService>

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
            val appAction: String? = when {
                path.startsWith("/download") -> "Download"
                path.startsWith("/github/project") -> "Push to GitHub"
                else -> null
            }
            if (appAction != null) {
                try {
                    extensions = extensionCatalog.get().checkAndMergeExtensions(queryParams["e"]?.toSet(), queryParams.getFirst("s"))
                    buildTool = queryParams.getFirst("b") ?: "MAVEN".toUpperCase()
                    extensions.forEach {
                        googleAnalyticsService.get().sendEvent(
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
                    googleAnalyticsService.get().sendEvent(
                            category = "App",
                            action = appAction,
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
            googleAnalyticsService.get().sendEvent(
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