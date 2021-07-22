package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.Stream
import io.quarkus.platform.tools.ToolsUtils
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
import java.util.logging.Level
import kotlin.Throws
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformService {

    @Inject
    lateinit var extensionProcessorConfig: ExtensionProcessorConfig
    private val catalogResolver = QuarkusProjectHelper.getCatalogResolver()
    private val platformServiceCacheRef: AtomicReference<PlatformServiceCache> = AtomicReference()

    fun onStart(@Observes e: StartupEvent?) {
        reload()
    }

    @Scheduled(cron = "{io.quarkus.code.quarkus-platforms.reload-cron-expr}")
    fun reload() {
        try {
            reloadPlatformServiceCache()
        } catch (e: RegistryResolutionException) {
            LOG.log(Level.SEVERE, "Could not resolve catalogs [" + e.message + "]", e)
        } catch (e: Exception) {
            LOG.log(Level.SEVERE, "Could not reload catalogs [" + e.message + "]", e)
        }
    }

    val isLoaded: Boolean
        get() = platformServiceCacheRef.get() != null && !recommendedCodeQuarkusExtensions.isNullOrEmpty()

    val platformsCache: PlatformServiceCache
        get() = platformServiceCacheRef.get() ?: error("Platforms cache must not be used if not loaded")

    val recommendedPlatformInfo: PlatformInfo
        get() {
            val pc = platformCatalog
            val defaultPlatformKey = pc.recommendedPlatform.platformKey
            val defaultStreamId = pc.recommendedPlatform.recommendedStream.id
            return getPlatformInfo(defaultPlatformKey, defaultStreamId)
                ?: error("Recommended platform should not be null")
        }

    val lastUpdated: LocalDateTime
        get() {
            return platformsCache.lastUpdated
        }

    val platformCatalog: PlatformCatalog
        get() {
            return platformsCache.platformCatalog
        }

    val recommendedCodeQuarkusExtensions: List<CodeQuarkusExtension>
        get() {
            val pc = platformCatalog
            val defaultPlatformKey = pc.recommendedPlatform.platformKey
            val defaultStreamId = pc.recommendedPlatform.recommendedStream.id
            return getCodeQuarkusExtensions(defaultPlatformKey, defaultStreamId)
                ?: error("Recommended extensions should not be null")
        }

    val recommendedStreamKey: String
        get() {
            val pc = platformCatalog
            val defaultPlatformKey = pc.recommendedPlatform.platformKey
            val defaultStreamId = pc.recommendedPlatform.recommendedStream.id
            return createStreamKey(defaultPlatformKey, defaultStreamId)
        }

    val streams: List<Stream>
        get() = platformServiceCacheRef.get().streamCatalogMap.map {
            Stream(
                key = it.key,
                quarkusCoreVersion = it.value.quarkusCoreVersion,
                recommended = it.value.recommended
            )
        }

    val streamKeys: Set<String>
        get() = platformServiceCacheRef.get().streamCatalogMap.keys

    fun getCodeQuarkusExtensions(platformKey: String, streamId: String): List<CodeQuarkusExtension>? {
        val key = createStreamKey(platformKey, streamId)
        return getCodeQuarkusExtensionsForStream(key)
    }

    fun getPlatformInfo(platformKey: String, streamId: String): PlatformInfo? {
        val key = createStreamKey(platformKey, streamId)
        return getPlatformInfoForStream(key)
    }

    fun getCodeQuarkusExtensionsForStream(stream: String): List<CodeQuarkusExtension>? {
        return getPlatformInfoForStream(stream)?.codeQuarkusExtensions
    }

    fun getPlatformInfoForStream(stream: String): PlatformInfo? {
        return if (platformServiceCacheRef.get().streamCatalogMap.containsKey(stream)) {
            platformServiceCacheRef.get().streamCatalogMap[stream]
        } else null
    }

    @Throws(RegistryResolutionException::class)
    private fun reloadPlatformServiceCache() {
        val platformCatalog = catalogResolver.resolvePlatformCatalog()
        val updatedStreamCatalogMap: MutableMap<String, PlatformInfo> = HashMap()
        val platforms = platformCatalog.platforms
        for (platform in platforms) {
            for (stream in platform.streams) {
                // Stream Map
                val recommendedRelease = stream.recommendedRelease
                // This is a temporary workaround for 2.0.3
                // val extensionCatalog = catalogResolver.resolveExtensionCatalog(recommendedRelease.memberBoms)
                val extensionCatalog = ToolsUtils.resolvePlatformDescriptorDirectly(
                    "io.quarkus", "quarkus-universe-bom", recommendedRelease.quarkusCoreVersion,
                    QuarkusProjectHelper.artifactResolver(), QuarkusProjectHelper.messageWriter()
                )
                val codeQuarkusExtensions: List<CodeQuarkusExtension> =
                    processExtensions(extensionCatalog, extensionProcessorConfig)
                val platformKey = platform.platformKey
                val streamId = stream.id
                val key = createStreamKey(platformKey, streamId)
                updatedStreamCatalogMap[key] = PlatformInfo(
                    key = key,
                    quarkusCoreVersion = stream.recommendedRelease.quarkusCoreVersion,
                    recommended = (stream.id == platform.recommendedStream.id),
                    codeQuarkusExtensions = codeQuarkusExtensions,
                    extensionCatalog = extensionCatalog
                )
            }
        }

        // Only replace the existing values if we successfully fetched new values
        if (updatedStreamCatalogMap.isEmpty()) {
            return
        }

        // Check if the recommended catalog is loaded
        val defaultPlatformKey = platformCatalog.recommendedPlatform.platformKey
        val defaultStreamId = platformCatalog.recommendedPlatform.recommendedStream.id
        val recommendedPlatform = updatedStreamCatalogMap[createStreamKey(defaultPlatformKey, defaultStreamId)]

        if (recommendedPlatform?.codeQuarkusExtensions.isNullOrEmpty()) {
            return
        }

        platformServiceCacheRef.set(
            PlatformServiceCache(
                platformCatalog,
                updatedStreamCatalogMap,
                LocalDateTime.now(ZoneOffset.UTC as ZoneId)
            )
        )

        LOG.info("PlatformService cache has been reloaded successfully")
    }

    private fun createStreamKey(platformKey: String, streamId: String): String {
        return platformKey + SEPARATOR + streamId
    }

    companion object {
        private val LOG = Logger.getLogger(PlatformService::class.java.name)
        const val SEPARATOR = ":"
    }
}