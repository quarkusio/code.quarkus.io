package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.misc.QuarkusExtensionUtils
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
import java.util.concurrent.atomic.AtomicReference
import kotlin.Throws
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformService {

    @Inject
    lateinit var extensionProcessorConfig: ExtensionProcessorConfig

    private val catalogResolver = QuarkusProjectHelper.getCatalogResolver()
    var streamCatalogMap: MutableMap<String, PlatformInfo> = HashMap()
    var platformCatalog: AtomicReference<PlatformCatalog> = AtomicReference()
    var lastUpdated: LocalDateTime? = null

    fun onStart(@Observes e: StartupEvent?) {
        reloadCatalogs()
    }

    @Scheduled(cron = "{reload.catalogs.cron.expr}")
    fun reloadCatalogs() {
        try {
            platformCatalog.set(catalogResolver.resolvePlatformCatalog())
            populateExtensionCatalogMaps()
            lastUpdated = LocalDateTime.now(ZoneOffset.UTC as ZoneId)
        } catch (e: RegistryResolutionException) {
            LOG.warning("Could not reload catalogs [" + e.message + "]")
        }
    }

    val platformInfo: PlatformInfo?
        get() {
            val pc = platformCatalog.get()
            val defaultPlatformKey = pc!!.recommendedPlatform.platformKey
            val defaultStreamId = pc!!.recommendedPlatform.recommendedStream.id
            return getPlatformInfo(defaultPlatformKey, defaultStreamId)
        }

    val extensionCatalog: List<CodeQuarkusExtension>?
        get() {
            val pc = platformCatalog.get()
            val defaultPlatformKey = pc!!.recommendedPlatform.platformKey
            val defaultStreamId = pc!!.recommendedPlatform.recommendedStream.id
            return getExtensionCatalog(defaultPlatformKey, defaultStreamId)
        }

    fun getDefaultStreamKey(): String {
        val pc = platformCatalog.get()
        val defaultPlatformKey = pc!!.recommendedPlatform.platformKey
        val defaultStreamId = pc!!.recommendedPlatform.recommendedStream.id
        return createStreamKey(defaultPlatformKey, defaultStreamId)
    }

    fun getExtensionCatalog(platformKey: String, streamId: String): List<CodeQuarkusExtension>? {
        val key = createStreamKey(platformKey, streamId)
        return getExtensionCatalogForStream(key)
    }

    fun getPlatformInfo(platformKey: String, streamId: String): PlatformInfo? {
        val key = createStreamKey(platformKey, streamId)
        return getPlatformInfoForStream(key)
    }

    fun getExtensionCatalogForStream(stream: String): List<CodeQuarkusExtension>? {
        return if (streamCatalogMap.containsKey(stream)) {
            streamCatalogMap[stream]?.codeQuarkusExtensions
        } else null
    }

    fun getPlatformInfoForStream(stream: String): PlatformInfo? {
        return if (streamCatalogMap.containsKey(stream)) {
            streamCatalogMap[stream]
        } else null
    }

    val streamKeys: Set<String>
        get() = streamCatalogMap.keys

    @Throws(RegistryResolutionException::class)
    private fun populateExtensionCatalogMaps() {
        var updatedStreamCatalogMap: MutableMap<String, PlatformInfo> = HashMap()
        val platforms = platformCatalog.get()!!.platforms
        for (platform in platforms) {
            for (stream in platform.streams) {
                // Stream Map
                val recommendedRelease = stream.recommendedRelease
                val extensionCatalog = catalogResolver.resolveExtensionCatalog(recommendedRelease.memberBoms)
                val codeQuarkusExtensions:List<CodeQuarkusExtension> = processExtensions(extensionCatalog, extensionProcessorConfig)
                val platformKey = platform.platformKey
                val streamId = stream.id
                val key = createStreamKey(platformKey, streamId)
                updatedStreamCatalogMap[key] = PlatformInfo(key,codeQuarkusExtensions,extensionCatalog)
            }
        }

        // Only replace the existing values if we successfully fetched new values
        if(updatedStreamCatalogMap.isNotEmpty()){
            this.streamCatalogMap.clear()
            this.streamCatalogMap.putAll(updatedStreamCatalogMap)
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