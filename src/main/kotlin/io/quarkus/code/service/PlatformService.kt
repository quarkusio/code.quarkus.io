package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.registry.catalog.PlatformCatalog
import java.time.LocalDateTime
import javax.enterprise.event.Observes
import io.quarkus.scheduler.Scheduled
import io.quarkus.registry.RegistryResolutionException
import kotlin.Throws
import io.quarkus.runtime.StartupEvent
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This service loads and store, in memory, the Extension catalogs for platforms
 * It updates on a certain schedule
 */
@Singleton
class PlatformService {

    companion object {
        private val LOG = java.util.logging.Logger.getLogger(PlatformService::class.java.name)
        const val SEPARATOR = ":"
    }

    @Inject
    lateinit var extensionProcessorConfig: ExtensionProcessorConfig

    private val catalogResolver = QuarkusProjectHelper.getCatalogResolver()
    lateinit var catalogMap: Map<String, List<CodeQuarkusExtension>>
    lateinit var platformCatalog: PlatformCatalog
    lateinit var lastUpdated: LocalDateTime

    fun onStart(@Observes e: StartupEvent) {
        reloadCatalogs()
    }

    @Scheduled(cron = "{reload.catalogs.cron.expr}")
    fun reloadCatalogs() {
        try {
            platformCatalog = catalogResolver.resolvePlatformCatalog()
            catalogMap = extensionCatalogMap
            lastUpdated = LocalDateTime.now(ZoneOffset.UTC)
        } catch (e: RegistryResolutionException) {
            LOG!!.warning ("Could not reload catalogs [" + e.message + "]")
        }
    }

    val extensionCatalog: List<CodeQuarkusExtension>
        get() {
            val defaultPlatformKey = platformCatalog!!.recommendedPlatform.platformKey
            return getExtensionCatalog(defaultPlatformKey)
        }

    fun getExtensionCatalog(platformKey: String): List<CodeQuarkusExtension> {
        val defaultStreamId = platformCatalog!!.getPlatform(platformKey).recommendedStream.id
        return getExtensionCatalog(platformKey, defaultStreamId)
    }

    fun getExtensionCatalog(platformKey: String, streamId: String): List<CodeQuarkusExtension> {
        val defaultReleaseVersion =
            platformCatalog!!.getPlatform(platformKey).getStream(streamId).recommendedRelease.version.toString()
        return getExtensionCatalog(platformKey, streamId, defaultReleaseVersion)
    }

    fun getExtensionCatalog(
        platformKey: Optional<String>,
        streamId: Optional<String>,
        releaseVersion: Optional<String>
    ): List<CodeQuarkusExtension> {
        val p = platformKey.orElse(platformCatalog!!.recommendedPlatform.platformKey)
        val s = streamId.orElse(platformCatalog!!.getPlatform(p).recommendedStream.id)
        val r =
            releaseVersion.orElse(platformCatalog!!.getPlatform(p).getStream(s).recommendedRelease.version.toString())
        return getExtensionCatalog(p, s, r)
    }

    fun getExtensionCatalog(
        platformKey: String,
        streamId: String,
        releaseVersion: String
    ): List<CodeQuarkusExtension> {
        val key = createKey(platformKey, streamId, releaseVersion)
        return if (catalogMap.containsKey(key)) {
            catalogMap[key]!!
        } else emptyList()
    }

    @get:Throws(RegistryResolutionException::class)
    private val extensionCatalogMap: Map<String, List<CodeQuarkusExtension>>
        private get() {
            val m: MutableMap<String, List<CodeQuarkusExtension>> = HashMap()
            val platforms = platformCatalog!!.platforms
            for (platform in platforms) {
                for (stream in platform.streams) {
                    for (release in stream.releases) {
                        val extensionCatalog = catalogResolver.resolveExtensionCatalog(release.memberBoms)
                        val codeQuarkusExtensions = QuarkusExtensionUtils.processExtensions(extensionCatalog, extensionProcessorConfig!!)
                        val key = createKey(platform.platformKey, stream.id, release.version.toString())
                        m[key] = codeQuarkusExtensions
                    }
                }
            }
            return m
        }

    private fun createKey(platformKey: String, streamId: String, releaseVersion: String): String {
        return platformKey + SEPARATOR + streamId + SEPARATOR + releaseVersion
    }
}