package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.config.PlatformConfig
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.model.Stream
import io.quarkus.registry.Constants
import java.util.HashMap
import io.quarkus.registry.catalog.PlatformCatalog
import java.time.LocalDateTime
import jakarta.enterprise.event.Observes
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import java.time.ZoneOffset
import java.time.ZoneId
import io.quarkus.registry.RegistryResolutionException
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import kotlin.Throws
import java.util.logging.Logger
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PlatformService {


    @Inject
    lateinit var platformConfig: PlatformConfig

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
        get() = getPlatformInfo()

    val cacheLastUpdated: LocalDateTime
        get() = platformsCache.cacheLastUpdated

    val platformCatalog: PlatformCatalog
        get() = platformsCache.platformCatalog

    val recommendedCodeQuarkusExtensions: List<CodeQuarkusExtension>
        get() = getCodeQuarkusExtensions(recommendedStreamKey)

    val recommendedStreamKey: String
        get() = platformsCache.recommendedStreamKey

    val streams: List<Stream>
        get() = platformServiceCacheRef.get().streams

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

    fun getPlatformInfo(streamKey: String? = null): PlatformInfo {
        val normalizedStreamKey = normalizeStreamKey(streamKey)
        return if (platformServiceCacheRef.get().streamCatalogMap.containsKey(normalizedStreamKey)) {
            platformServiceCacheRef.get().streamCatalogMap[normalizedStreamKey]!!
        } else throw IllegalArgumentException("Invalid streamKey: $streamKey")
    }

    @Throws(RegistryResolutionException::class)
    private fun reloadPlatformServiceCache() {
        catalogResolver.clearRegistryCache()
        val platformCatalog: PlatformCatalog? = if(platformConfig.registryId.isEmpty) catalogResolver.resolvePlatformCatalog()
            else catalogResolver.resolvePlatformCatalogFromRegistry(platformConfig.registryId.get())
        val updatedStreamCatalogMap: MutableMap<String, PlatformInfo> = HashMap()
        if(platformCatalog?.metadata == null || platformCatalog.platforms == null) {
            throw error("Platform catalog not found");
        }

        val platformTimestamp: String? = platformCatalog.metadata[Constants.LAST_UPDATED]?.toString()
        if(platformTimestamp.isNullOrBlank()) {
            throw error("Platform last updated date is empty");
        }
        if (platformServiceCacheRef.get()?.platformTimestamp == platformTimestamp) {
           LOG.log(Level.INFO, "The platform cache is up to date with the registry")
           return
        }
        val platforms = platformCatalog.platforms
        val streams = arrayListOf<Stream>()
        for (platform in platforms) {
            for (stream in platform.streams) {
                // Stream Map
                val recommendedRelease = stream.recommendedRelease
                val extensionCatalog = catalogResolver.resolveExtensionCatalog(recommendedRelease.memberBoms)
                val codeQuarkusExtensions: List<CodeQuarkusExtension> =
                    processExtensions(extensionCatalog)
                val platformKey = platform.platformKey
                val streamId = stream.id
                val streamKey = createStreamKey(platformKey, streamId)
                val platformInfo = PlatformInfo(
                    platformKey = platformKey,
                    streamKey = streamKey,
                    quarkusCoreVersion = stream.recommendedRelease.quarkusCoreVersion,
                    platformVersion = stream.recommendedRelease.version.toString(),
                    recommended = (stream.id == platform.recommendedStream.id),
                    codeQuarkusExtensions = codeQuarkusExtensions,
                    extensionCatalog = extensionCatalog
                )
                streams.add(Stream(
                    key = streamKey,
                    quarkusCoreVersion = platformInfo.quarkusCoreVersion,
                    platformVersion = stream.recommendedRelease.version.toString(),
                    recommended = platformInfo.recommended,
                    status = getStreamStatus(platformInfo)
                ))
                updatedStreamCatalogMap[streamKey] = platformInfo
            }
        }
        val newCache = PlatformServiceCache(
            recommendedStreamKey = createStreamKey(
                platformCatalog.recommendedPlatform.platformKey,
                platformCatalog.recommendedPlatform.recommendedStream.id
            ),
            platformCatalog = platformCatalog,
            streamCatalogMap = updatedStreamCatalogMap,
            cacheLastUpdated = LocalDateTime.now(ZoneOffset.UTC as ZoneId),
            platformTimestamp = platformTimestamp,
            streams = streams
        )

        checkNewCache(newCache)

        platformServiceCacheRef.set(newCache)
        LOG.log(Level.INFO) {
            """
            PlatformService cache has been reloaded successfully:
                platform timestamp: $platformTimestamp
                recommended stream key: $recommendedStreamKey (core: ${recommendedPlatformInfo.quarkusCoreVersion})
                recommended stream extensions: ${recommendedCodeQuarkusExtensions.size}
                available streams: ${updatedStreamCatalogMap.keys.joinToString(", ")}
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

    private fun getStreamStatus(platformInfo: PlatformInfo): String? {
        return DefaultArtifactVersion(platformInfo.quarkusCoreVersion).qualifier?.uppercase()
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
        val streams: List<Stream>,
        val platformCatalog: PlatformCatalog,
        val streamCatalogMap: MutableMap<String, PlatformInfo>,
        val cacheLastUpdated: LocalDateTime,
        val platformTimestamp: String
    )
}