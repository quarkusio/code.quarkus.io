package io.quarkus.code.services

import com.google.common.base.Preconditions.checkState
import io.quarkus.platform.descriptor.resolver.json.QuarkusJsonPlatformDescriptorResolver
import io.quarkus.platform.tools.config.QuarkusPlatformConfig
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import java.util.stream.Collectors
import javax.inject.Singleton

@Singleton
open class QuarkusExtensionCatalog {

    companion object {
        @JvmStatic
        internal val platformVersion = ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-platform-version", String::class.java)

        @JvmStatic
        internal val bundledQuarkusVersion = ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-version", String::class.java)

        @JvmStatic
        internal val descriptor = QuarkusJsonPlatformDescriptorResolver.newInstance().resolveFromBom("io.quarkus", "quarkus-universe-bom", platformVersion)

        @JvmStatic
        internal val processedExtensions = QuarkusExtensionUtils.processExtensions(descriptor)

        init {
            checkState(platformVersion.isNotEmpty()) { "io.quarkus.code.quarkus-platform-version must not be null or empty" }
            checkState(bundledQuarkusVersion.isNotEmpty()) { "io.quarkus.code.quarkus-version must not be null or empty" }
            checkState(descriptor.quarkusVersion == bundledQuarkusVersion, "The platform version (%s) must be compatible with the bundled Quarkus version (%s != %s)", descriptor.bomVersion, descriptor.quarkusVersion, bundledQuarkusVersion)
            if (!QuarkusPlatformConfig.hasGlobalDefault()) {
                QuarkusPlatformConfig.defaultConfigBuilder().setPlatformDescriptor(descriptor).build()
            }
        }

        fun checkPlatformInitialization() {
            check(QuarkusPlatformConfig.hasGlobalDefault()) { "Quarkus platform must be initialized" }
        }
    }

    val extensions = processedExtensions

    val extensionsByShortId = processedExtensions.associateBy { it.shortId }
    val extensionsById = processedExtensions.associateBy { it.id }

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