package io.quarkus.code.rest

import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.service.GoogleAnalyticsService
import io.quarkus.code.service.PlatformService
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.BufferedReader
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors
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
        private val LOG = Logger.getLogger(AnalyticsFilter::class.java.name)
    }

    @Inject
    lateinit var googleAnalyticsService: Instance<GoogleAnalyticsService>

    @Inject
    lateinit var platformService: Instance<PlatformService>

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
            val appAction: String? = when {
                path.startsWith("/download") -> "Download"
                path.startsWith("/github/project") -> "Push to GitHub"
                else -> null
            }
            if (appAction != null) {
                try {
                    val w = readWatchedData(context)
                    w.extensions.forEach {
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
                                extensions = w.extensions,
                                buildTool = w.buildTool
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
                            extensions = w.extensions,
                            buildTool = w.buildTool
                    )

                } catch (e: IllegalStateException) {
                    LOG.log(Level.FINE, e) { "Error while extracting extension list from request" }
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
            LOG.log(Level.SEVERE, e) { "Error while generating/sending an analytic event" }
        }

    }

    private fun readWatchedData(context: ContainerRequestContext): WatchedData {
        val queryParams = context.uriInfo.queryParameters
        val extensions: Set<String>
        val buildTool: String
        if(httpRequest!!.method == "POST") {
            val text = context.entityStream.bufferedReader().use(BufferedReader::readText)
            context.entityStream = text.byteInputStream()
            if(text.isNotBlank()) {
                val json = JsonObject(text)
                val rawExtensions = json.getJsonArray("extensions", JsonArray()).stream().map { it.toString() }.collect(Collectors.toSet())
                extensions = platformService.get().recommendedPlatformInfo.checkAndMergeExtensions(rawExtensions)
                buildTool = json.getString("buildTool", ProjectDefinition.DEFAULT_BUILDTOOL)
            } else {
                extensions = emptySet()
                buildTool = ProjectDefinition.DEFAULT_BUILDTOOL
            }
        } else {
            extensions = platformService.get().recommendedPlatformInfo.checkAndMergeExtensions(queryParams["e"]?.toSet(), queryParams.getFirst("s"))
            buildTool = queryParams.getFirst("b") ?: ProjectDefinition.DEFAULT_BUILDTOOL
        }
        return WatchedData(
            buildTool = buildTool,
            extensions = extensions
        )
    }

    data class WatchedData(val buildTool: String, val extensions: Set<String>)
}