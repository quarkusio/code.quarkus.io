package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.code.model.CodeQuarkusExtension
import java.util.HashMap
import io.quarkus.registry.catalog.PlatformCatalog
import java.time.LocalDateTime
import javax.enterprise.event.Observes
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import java.time.ZoneOffset
import java.time.ZoneId
import io.quarkus.registry.RegistryResolutionException
import kotlin.Throws
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformService {
    @Inject
    var extensionProcessorConfig: ExtensionProcessorConfig? = null
    private val catalogResolver = QuarkusProjectHelper.getCatalogResolver()
    var streamCatalogMap: MutableMap<String, List<CodeQuarkusExtension>> = HashMap()
    var platformCatalog: PlatformCatalog? = null
    var lastUpdated: LocalDateTime? = null

    fun onStart(@Observes e: StartupEvent?) {
        reloadCatalogs()
    }

    @Scheduled(cron = "{reload.catalogs.cron.expr}")
    fun reloadCatalogs() {
        try {
            platformCatalog = catalogResolver.resolvePlatformCatalog()
            populateExtensionCatalogMaps()
            lastUpdated = LocalDateTime.now(ZoneOffset.UTC as ZoneId)
        } catch (e: RegistryResolutionException) {
            LOG.warning("Could not reload catalogs [" + e.message + "]")
        }
    }

    val extensionCatalog: List<CodeQuarkusExtension>?
        get() {
            val defaultPlatformKey = platformCatalog!!.recommendedPlatform.platformKey
            val defaultStreamId = platformCatalog!!.recommendedPlatform.recommendedStream.id
            return getExtensionCatalog(defaultPlatformKey, defaultStreamId)
        }

    fun getExtensionCatalog(platformKey: String, streamId: String): List<CodeQuarkusExtension>? {
        val key = createStreamKey(platformKey, streamId)
        return getExtensionCatalogForStream(key)
    }

    fun getExtensionCatalogForStream(stream: String): List<CodeQuarkusExtension>? {
        return if (streamCatalogMap.containsKey(stream)) {
            streamCatalogMap[stream]
        } else null
    }

    val streamKeys: Set<String>
        get() = streamCatalogMap.keys

    @Throws(RegistryResolutionException::class)
    private fun populateExtensionCatalogMaps() {
        streamCatalogMap.clear()
        val platformCatalog = platformCatalog
        val platforms = platformCatalog!!.platforms
        for (platform in platforms) {
            for (stream in platform.streams) {
                // Stream Map
                val recommendedRelease = stream.recommendedRelease
                val extensionCatalog = catalogResolver.resolveExtensionCatalog(recommendedRelease.memberBoms)
                val codeQuarkusExtensions = processExtensions(extensionCatalog, extensionProcessorConfig!!)
                val platformKey = platform.platformKey
                val streamId = stream.id
                val key = createStreamKey(platformKey, streamId)
                streamCatalogMap[key] = codeQuarkusExtensions
            }
        }
    }

    private fun createStreamKey(platformKey: String, streamId: String): String {
        return platformKey + SEPARATOR + streamId
    }

    companion object {
        private val LOG = Logger.getLogger(PlatformService::class.java.name)
        const val SEPARATOR = ":"
    }
}