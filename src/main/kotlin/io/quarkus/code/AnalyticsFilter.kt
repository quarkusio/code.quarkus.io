package io.quarkus.code

import com.brsanthu.googleanalytics.GoogleAnalytics
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.stream.Collectors
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider

@Provider
class AnalyticsFilter : ContainerRequestFilter {

    @Context
    internal var info: UriInfo? = null

    @ConfigProperty(name = "io.quarkus.code.google-tracking-id")
    lateinit var googleTrackingId: String

    override fun filter(context: ContainerRequestContext) {
        val googleAnalytics: GoogleAnalytics = GoogleAnalytics.builder()
                .withTrackingId(googleTrackingId)
                .withConfig(GoogleAnalyticsConfig().setUserAgent("userAgent"))
                .build()

        val stream = context.uriInfo.queryParameters.entries.stream()
        val params = stream.map { entry -> "${entry.key}=${entry.value}" }.collect(Collectors.joining("&"))
        val path = info!!.path
        val divider = if (params != null) "?" else ""

        googleAnalytics.pageView().documentPath("$path$divider${params ?: ""}").sendAsync()
    }
}