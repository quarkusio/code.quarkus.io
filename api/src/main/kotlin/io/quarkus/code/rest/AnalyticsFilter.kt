package io.quarkus.code.rest

import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.service.PlatformService
import io.quarkus.code.service.SegmentAnalyticsService
import io.quarkus.maven.dependency.ArtifactCoords
import io.quarkus.maven.dependency.ArtifactKey
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.BufferedReader
import java.net.InetAddress
import java.util.*
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
    lateinit var analyticsService: Instance<SegmentAnalyticsService>

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
            val source = queryParams.getFirst("cn") ?: context.getHeaderString("Client-Name") ?: "unknown"
            val url = info!!.requestUri.toString()
            val userAgent = context.headers.getFirst(HttpHeaders.USER_AGENT)
            val referer = context.headers.getFirst("Referer")
            val remoteAddr = httpRequest!!.remoteAddr
            val inetAddress = InetAddress.getByName(remoteAddr)
            val address = inetAddress.address
            val uuid = UUID.nameUUIDFromBytes(address)
            val anonymousId = uuid.toString()
            val appAction: String? = when {
                path.startsWith("/download") -> "App Download"
                path.startsWith("/github/project") -> "App Push to GitHub"
                else -> null
            }
            if (appAction != null) {
                try {
                    val w = readWatchedData(context, source)
                    (w["extensions"] as? Set<*>)?.forEach {
                        val id = it as String
                        val props = if (id.split(":").size == 2) {
                            val key = ArtifactKey.fromString(id)
                            mapOf(
                                "extension" to key.toGacString()
                            )
                        } else {
                            val coords = ArtifactCoords.fromString(id)
                            mapOf(
                                "extension" to coords.key.toGacString(),
                                "extensionVersion" to coords.version,
                            )
                        }
                        analyticsService.get().track(
                            event = "Extension Used",
                            properties = w - "extensions" + props,
                            source = source,
                            path = path,
                            url = url,
                            userAgent = userAgent,
                            referer = referer,
                            anonymousId = anonymousId,
                        )
                    }
                    analyticsService.get().track(
                        event = appAction,
                        source = source,
                        path = path,
                        url = url,
                        userAgent = userAgent,
                        referer = referer,
                        anonymousId = anonymousId,
                        properties = w
                    )

                } catch (e: IllegalStateException) {
                    LOG.log(Level.FINE, e) { "Error while extracting extension list from request" }
                }
            }
            analyticsService.get().track(
                event = "Api Call",
                source = source,
                path = path,
                url = url,
                userAgent = userAgent,
                referer = referer,
                anonymousId = anonymousId

            )
        } catch (e: Exception) {
            LOG.log(Level.SEVERE, e) { "Error while generating/sending an analytic event" }
        }

    }

    private fun readWatchedData(context: ContainerRequestContext, source: String): Map<String, Any?> {
        val queryParams = context.uriInfo.queryParameters
        val extensions: Set<String>
        val buildTool: String
        val streamKey: String?
        val javaVersion: String
        val noCode: Boolean
        if (httpRequest!!.method == "POST") {
            val text = context.entityStream.bufferedReader().use(BufferedReader::readText)
            context.entityStream = text.byteInputStream()
            if (text.isNotBlank()) {
                val json = JsonObject(text)
                val rawExtensions = json.getJsonArray("extensions", JsonArray()).stream().map { it.toString() }
                    .collect(Collectors.toSet())
                extensions = platformService.get().recommendedPlatformInfo.checkAndMergeExtensions(rawExtensions)
                buildTool = json.getString("buildTool", ProjectDefinition.DEFAULT_BUILDTOOL)
                javaVersion = json.getString("javaVersion", ProjectDefinition.DEFAULT_JAVA_VERSION)
                streamKey = json.getString("streamKey")
                noCode = json.getBoolean("noCode")
            } else {
                extensions = emptySet()
                buildTool = ProjectDefinition.DEFAULT_BUILDTOOL
                streamKey = null
                javaVersion = ProjectDefinition.DEFAULT_JAVA_VERSION
                noCode = ProjectDefinition.DEFAULT_NO_CODE
            }
        } else {
            extensions =
                platformService.get().recommendedPlatformInfo.checkAndMergeExtensions(queryParams["e"]?.toSet())
            buildTool = queryParams.getFirst("b") ?: ProjectDefinition.DEFAULT_BUILDTOOL
            streamKey = queryParams.getFirst("S")
            javaVersion = queryParams.getFirst("j") ?: ProjectDefinition.DEFAULT_JAVA_VERSION
            noCode = queryParams.getFirst("nc")?.toBoolean() ?: ProjectDefinition.DEFAULT_NO_CODE
        }
        val resolvedStreamKey = platformService.get().getPlatformInfo(streamKey).streamKey
        return mapOf(
            "buildTool" to buildTool,
            "extensions" to extensions,
            "streamKey" to resolvedStreamKey,
            "javaVersion" to javaVersion,
            "noCode" to noCode
        )
    }

}