package io.quarkus.code.services

import com.google.common.base.Preconditions.checkState
import io.quarkus.code.CodeQuarkusResource
import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.config.QuarkusPlatformConfig
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor
import io.quarkus.platform.descriptor.resolver.json.QuarkusJsonPlatformDescriptorResolver
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarkusExtensionCatalog {

    companion object {
        private val LOG = Logger.getLogger(QuarkusExtensionCatalog::class.java.name)

        @JvmStatic
        internal val platformGroupId = ConfigProviderResolver.instance().getConfig().getOptionalValue("io.quarkus.code.quarkus-platform.group-id", String::class.java).orElse("io.quarkus")

        @JvmStatic
        internal val platformArtifactId = ConfigProviderResolver.instance().getConfig().getOptionalValue("io.quarkus.code.quarkus-platform.artifact-id", String::class.java).orElse("quarkus-universe-bom")

        @JvmStatic
        internal val platformVersion = ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-platform.version", String::class.java)

        @JvmStatic
        internal val bundledQuarkusVersion = ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-version", String::class.java)

        @JvmStatic
        internal val descriptor = QuarkusJsonPlatformDescriptorResolver.newInstance().resolveFromBom(platformGroupId, platformArtifactId, platformVersion)

        init {
            checkState(platformVersion.isNotEmpty()) { "io.quarkus.code.quarkus-platform-version must not be null or empty" }
            checkState(bundledQuarkusVersion.isNotEmpty()) { "io.quarkus.code.quarkus-version must not be null or empty" }
            checkState(descriptor.quarkusVersion == bundledQuarkusVersion, "The platform version (%s) must be compatible with the bundled Quarkus version (%s != %s)", descriptor.bomVersion, descriptor.quarkusVersion, bundledQuarkusVersion)
            if (!io.quarkus.platform.tools.config.QuarkusPlatformConfig.hasGlobalDefault()) {
                io.quarkus.platform.tools.config.QuarkusPlatformConfig.defaultConfigBuilder().setPlatformDescriptor(descriptor).build()
            }
        }

        fun checkPlatformInitialization() {
            check(io.quarkus.platform.tools.config.QuarkusPlatformConfig.hasGlobalDefault()) { "Quarkus platform must be initialized" }
        }
    }

    @Inject
    lateinit var config: CodeQuarkusConfig

    @Inject
    lateinit var platformConfig: QuarkusPlatformConfig

    @Inject
    lateinit var extensionProcessorConfig: ExtensionProcessorConfig

    lateinit var extensions: List<CodeQuarkusExtension>

    lateinit var extensionsByShortId: Map<String, CodeQuarkusExtension>
    lateinit var extensionsById: Map<String, CodeQuarkusExtension>

    fun onStart(@Observes e: StartupEvent) {
        extensions = QuarkusExtensionUtils.processExtensions(descriptor, extensionProcessorConfig)
        extensionsByShortId = extensions.associateBy { it.shortId }
        extensionsById = extensions.associateBy { it.id }
        LOG.log(Level.INFO) {"""
            Extensions Catalog has been processed with ${extensions.size} extensions:
                Quarkus platform: $platformGroupId:$platformArtifactId:$platformVersion
                tagsFrom: ${extensionProcessorConfig.tagsFrom}
        """.trimIndent()}
    }


    fun checkAndMergeExtensions(extensionsIds: Set<String>?, rawShortExtensions: String?): Set<String> {
        val fromId = (extensionsIds ?: setOf())
                .stream()
                .filter { !it.isBlank() }
                .map { (this.extensionsById[it] ?: error("Invalid extension: $it")).id }
                .collect(Collectors.toSet())
        val fromShortId = parseShortExtensions(rawShortExtensions).stream()
                .map { (this.extensionsByShortId[it] ?: error("Invalid shortId: $it")).id }
                .collect(Collectors.toSet())
        return fromId union fromShortId
    }

    private fun parseShortExtensions(shortExtension: String?): Set<String> {
        return if (shortExtension.isNullOrBlank()) {
            setOf()
        } else {
            shortExtension.split(".").filter { !it.isBlank() }.toSet()
        }
    }

}