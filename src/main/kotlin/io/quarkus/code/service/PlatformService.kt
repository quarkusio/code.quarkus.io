package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.model.Stream
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

    @Inject
    lateinit var projectService: QuarkusProjectService

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
            return getCodeQuarkusExtensions(recommendedStreamKey)
        }

    val recommendedStreamKey: String
        get() {
            return recommendedPlatformInfo.streamKey
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
        return getCodeQuarkusExtensions(key)
    }

    fun getCodeQuarkusExtensions(streamKey: String): List<CodeQuarkusExtension> {
        return getPlatformInfo(streamKey).codeQuarkusExtensions
    }


    fun getPlatformInfo(platformKey: String, streamId: String): PlatformInfo? {
        val key = createStreamKey(platformKey, streamId)
        return getPlatformInfo(key)
    }

    fun getPlatformInfo(streamKey: String?): PlatformInfo {
        val normalizedStreamKey = normalizeStreamKey(streamKey)
        return if (platformServiceCacheRef.get().streamCatalogMap.containsKey(normalizedStreamKey)) {
            platformServiceCacheRef.get().streamCatalogMap[normalizedStreamKey]!!
        } else throw IllegalArgumentException("Invalid streamKey: $streamKey")
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
                val extensionCatalog = catalogResolver.resolveExtensionCatalog(recommendedRelease.memberBoms)
                val codeQuarkusExtensions: List<CodeQuarkusExtension> =
                    processExtensions(extensionCatalog, extensionProcessorConfig)
                val platformKey = platform.platformKey
                val streamId = stream.id
                val streamKey = createStreamKey(platformKey, streamId)
                updatedStreamCatalogMap[streamKey] = PlatformInfo(
                    platformKey = platformKey,
                    streamKey = streamKey,
                    quarkusCoreVersion = stream.recommendedRelease.quarkusCoreVersion,
                    recommended = (stream.id == platform.recommendedStream.id),
                    codeQuarkusExtensions = codeQuarkusExtensions,
                    extensionCatalog = extensionCatalog
                )
            }
        }
        val newCache = PlatformServiceCache(
            createStreamKey(
                platformCatalog.recommendedPlatform.platformKey,
                platformCatalog.recommendedPlatform.recommendedStream.id
            ),
            platformCatalog,
            updatedStreamCatalogMap,
            LocalDateTime.now(ZoneOffset.UTC as ZoneId)
        )

        checkNewCache(newCache)

        platformServiceCacheRef.set(newCache)
        LOG.log(Level.INFO) {
            """
            PlatformService cache has been reloaded successfully:
                recommendedStreamKey: $recommendedStreamKey (core: ${recommendedPlatformInfo.quarkusCoreVersion})
                number of extensions: ${recommendedCodeQuarkusExtensions.size}
        """.trimIndent()
        }
    }

    private fun checkNewCache(newCache: PlatformServiceCache) {
        // Only replace the existing values if we successfully fetched new values
        if (newCache.streamCatalogMap.isEmpty()) {
            throw error("No stream found")
        }

        // Check streams
        if (!newCache.streamCatalogMap.containsKey(newCache.recommendedStreamKey)) {
            throw error("Recommended stream not found in stream catalog: " + newCache.recommendedStreamKey)
        }

        for (entry in newCache.streamCatalogMap) {
            if (entry.value.codeQuarkusExtensions.isNullOrEmpty()) {
                throw error("No extension found in the stream: " + entry.key)
            }
            projectService.createTmp(
                platformInfo = entry.value,
                projectDefinition = ProjectDefinition(
                    streamKey = entry.key,
                    extensions = hashSetOf("resteasy", "resteasy-jackson", "hibernate-validator")
                ),
                isGitHub = false,
                silent = true
            )
            projectService.createTmp(
                platformInfo = entry.value,
                projectDefinition = ProjectDefinition(
                    streamKey = entry.key,
                    extensions = hashSetOf("resteasy-reactive", "resteasy-reactive-jackson", "hibernate-validator")
                ),
                isGitHub = false,
                silent = true
            )
            projectService.createTmp(
                platformInfo = entry.value,
                projectDefinition = ProjectDefinition(streamKey = entry.key, extensions = hashSetOf("spring-web")),
                isGitHub = false,
                silent = true
            )
        }

    }

    private fun createStreamKey(platformKey: String, streamId: String): String {
        return platformKey + SEPARATOR + streamId
    }

    private fun normalizeStreamKey(streamKey: String?): String {
        if (streamKey == null) {
            return recommendedStreamKey
        }
        return if (streamKey.contains(":")) streamKey else createStreamKey(
            recommendedPlatformInfo.platformKey,
            streamKey
        )
    }

    companion object {
        private val LOG = Logger.getLogger(PlatformService::class.java.name)
        const val SEPARATOR = ":"
    }

    data class PlatformServiceCache(
        val recommendedStreamKey: String,
        val platformCatalog: PlatformCatalog,
        val streamCatalogMap: MutableMap<String, PlatformInfo>,
        val lastUpdated: LocalDateTime
    )
}